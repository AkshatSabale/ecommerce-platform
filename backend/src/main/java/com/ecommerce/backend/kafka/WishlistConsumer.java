package com.ecommerce.backend.kafka;

import com.ecommerce.backend.model.Wishlist;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.WishlistRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class WishlistConsumer {

  @Autowired
  private WishlistRepository wishlistRepo;

  @Autowired
  private ProductRepository productRepo;

  @Autowired
  private CacheManager cacheManager; // for cache eviction

  @KafkaListener(topics = "wishlist-topic", groupId = "wishlist_group")
  public void consume(String json) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      WishlistMessage msg = mapper.readValue(json, WishlistMessage.class);

      Wishlist wishlist = wishlistRepo.findByUserId(msg.getUserId())
          .orElseGet(() -> {
            Wishlist w = new Wishlist();
            w.setUserId(msg.getUserId());
            return w;
          });

      switch (msg.getOperation()) {
        case "ADD":
          if (msg.getProductId() != null && productRepo.existsById(msg.getProductId())) {
            wishlist.getProductIds().add(msg.getProductId());
            wishlistRepo.save(wishlist);
          }
          break;

        case "REMOVE":
          if (msg.getProductId() != null) {
            wishlist.getProductIds().remove(msg.getProductId());
            wishlistRepo.save(wishlist);
          }
          break;

        case "CLEAR":
          wishlist.getProductIds().clear();
          wishlistRepo.save(wishlist);
          break;

        default:
          System.out.println("Unknown wishlist operation: " + msg.getOperation());
      }

      // Evict cache entry for this userId after changes
      Cache cache = cacheManager.getCache("wishlists");
      if (cache != null) {
        cache.evict(msg.getUserId());
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}