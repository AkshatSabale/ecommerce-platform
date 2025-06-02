
import React, { useState, useEffect } from 'react';
import { DatePicker } from 'antd';
import { Line, Bar } from 'react-chartjs-2';
import api from '../services/api';
import 'chart.js/auto';
import dayjs, { Dayjs } from 'dayjs';

const { RangePicker } = DatePicker;

interface DailyRevenueData {
  date: string;
  revenue: number;
}

interface TopProduct {
  productId: number;
  productName: string;
  quantity: number;
}

interface RevenueData {
  amount: number;
}

const AdminDashboardAnalytics = () => {
  const [dateRange, setDateRange] = useState<[Dayjs, Dayjs]>([dayjs().subtract(30, 'day'), dayjs()]);
  const [topProducts, setTopProducts] = useState<TopProduct[]>([]);
  const [revenue, setRevenue] = useState<number>(0);
  const [dailyRevenue, setDailyRevenue] = useState<DailyRevenueData[]>([]);
  const [loading, setLoading] = useState(false);

  const fetchAnalytics = async () => {
    setLoading(true);
    try {
      const [startDate, endDate] = dateRange;

      const [productsRes, revenueRes, dailyRes] = await Promise.all([
        api.get<TopProduct[]>('/api/analytics/top-products', {
          params: {
            limit: 10,
            startDate: startDate?.format('YYYY-MM-DD'),
            endDate: endDate?.format('YYYY-MM-DD')
          }
        }),
        api.get<RevenueData>('/api/analytics/revenue', {
          params: {
            startDate: startDate?.format('YYYY-MM-DD'),
            endDate: endDate?.format('YYYY-MM-DD')
          }
        }),
        api.get<DailyRevenueData[]>('/api/analytics/revenue/daily', {
          params: {
            startDate: startDate?.format('YYYY-MM-DD'),
            endDate: endDate?.format('YYYY-MM-DD')
          }
        })
      ]);

      setTopProducts(productsRes.data);
      setRevenue(revenueRes.data.amount);
      setDailyRevenue(dailyRes.data);
    } catch (error) {
      console.error('Error fetching analytics:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAnalytics();
  }, [dateRange]);

  const dailyRevenueChart = {
    labels: dailyRevenue.map(item => dayjs(item.date).format('MMM D')),
    datasets: [
      {
        label: 'Daily Revenue',
        data: dailyRevenue.map(item => item.revenue),
        backgroundColor: 'rgba(54, 162, 235, 0.2)',
        borderColor: 'rgba(54, 162, 235, 1)',
        borderWidth: 1,
        tension: 0.1
      }
    ]
  };

  const topProductsChart = {
    labels: topProducts.map(item => item.productName || `Product ${item.productId}`),
    datasets: [
      {
        label: 'Units Sold',
        data: topProducts.map(item => item.quantity),
        backgroundColor: 'rgba(255, 99, 132, 0.2)',
        borderColor: 'rgba(255, 99, 132, 1)',
        borderWidth: 1
      }
    ]
  };

  return (
    <div className="p-6">
      <div className="mb-6">
        <h2 className="text-2xl font-bold mb-4">Analytics Dashboard</h2>
        <div className="flex items-center space-x-4 mb-4">
          <RangePicker
            value={dateRange}
            onChange={(dates: [Dayjs | null, Dayjs | null] | null) => {
              if (dates && dates[0] && dates[1]) {
                setDateRange([dates[0], dates[1]]);
              }
            }}
            disabledDate={(current) => current && current > dayjs().endOf('day')}
          />
          <button
            onClick={fetchAnalytics}
            className="bg-blue-500 text-white px-4 py-2 rounded"
            disabled={loading}
          >
            {loading ? 'Loading...' : 'Refresh'}
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
        <div className="bg-white p-4 rounded shadow">
          <h3 className="text-lg font-semibold mb-4">Total Revenue</h3>
          <div className="text-3xl font-bold text-green-600">
            ${revenue.toFixed(2)}
          </div>
          <div className="text-sm text-gray-500 mt-1">
            {dateRange[0].format('MMM D, YYYY')} - {dateRange[1].format('MMM D, YYYY')}
          </div>
        </div>

        <div className="bg-white p-4 rounded shadow">
          <h3 className="text-lg font-semibold mb-4">Top Products</h3>
          {topProducts.length > 0 ? (
            <div className="space-y-2">
              {topProducts.map((product, index) => (
                <div key={index} className="flex justify-between">
                  <span>{product.productName || `Product ${product.productId}`}</span>
                  <span className="font-medium">{product.quantity} sold</span>
                </div>
              ))}
            </div>
          ) : (
            <p>No data available</p>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 gap-6">
        <div className="bg-white p-4 rounded shadow">
          <h3 className="text-lg font-semibold mb-4">Revenue Trend</h3>
          <Line
            data={dailyRevenueChart}
            options={{
              responsive: true,
              plugins: {
                legend: {
                  position: 'top',
                },
                tooltip: {
                  callbacks: {
                    label: (context) => `$${(context.parsed.y as number).toFixed(2)}`
                  }
                }
              },
              scales: {
                y: {
                  beginAtZero: true,
                  ticks: {
                    callback: (value: number | string) => `$${value}`
                  }
                }
              }
            }}
          />
        </div>

        <div className="bg-white p-4 rounded shadow">
          <h3 className="text-lg font-semibold mb-4">Top Selling Products</h3>
          <Bar
            data={topProductsChart}
            options={{
              responsive: true,
              plugins: {
                legend: {
                  display: false
                }
              },
              scales: {
                y: {
                  beginAtZero: true
                }
              }
            }}
          />
        </div>
      </div>
    </div>
  );
};

export default AdminDashboardAnalytics;