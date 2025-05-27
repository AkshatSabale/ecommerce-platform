import rateLimit from 'express-rate-limit';

export const generalLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 100,
  message: 'Too many requests from this IP, please try again later.'
});

export const loginLimiter = rateLimit({
  windowMs: 10 * 60 * 1000,
  max: 5,
  message: 'Too many login attempts, please try again after 10 minutes.'
});

export const searchLimiter = rateLimit({
  windowMs: 60 * 1000,
  max: 20,
  message: 'Too many search requests, slow down a bit.'
});