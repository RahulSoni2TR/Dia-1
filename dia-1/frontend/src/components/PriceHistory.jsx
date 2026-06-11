import React, { useState, useEffect, useRef } from 'react';
import './PriceHistory.css';

const API_BASE = import.meta.env.DEV ? 'http://localhost:8080' : '';

function PriceHistory({ onSwitchPage }) {
  const [commodity, setCommodity] = useState('24.00');
  const [period, setPeriod] = useState('1y');
  const [showCustomRange, setShowCustomRange] = useState(false);
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [showTable, setShowTable] = useState(false);
  const [historyData, setHistoryData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [chartLoaded, setChartLoaded] = useState(false);

  const canvasRef = useRef(null);
  const chartInstanceRef = useRef(null);

  // Load Chart.js script dynamically
  useEffect(() => {
    if (window.Chart) {
      setChartLoaded(true);
      return;
    }

    const script = document.createElement('script');
    script.src = 'https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js';
    script.async = true;
    script.onload = () => {
      setChartLoaded(true);
    };
    script.onerror = () => {
      console.error('Failed to load Chart.js from CDN.');
    };
    document.body.appendChild(script);

    return () => {
      // Clean up script if component unmounts before loading
      if (document.body.contains(script)) {
        document.body.removeChild(script);
      }
    };
  }, []);

  // Fetch history data when filters change
  useEffect(() => {
    fetchHistory();
  }, [commodity, period, fromDate, toDate, showCustomRange]);

  // Render chart when historyData or chartLoaded or showTable change
  useEffect(() => {
    if (!chartLoaded || showTable || historyData.length === 0 || !canvasRef.current) {
      if (chartInstanceRef.current) {
        chartInstanceRef.current.destroy();
        chartInstanceRef.current = null;
      }
      return;
    }

    const ctx = canvasRef.current.getContext('2d');
    if (chartInstanceRef.current) {
      chartInstanceRef.current.destroy();
    }

    const labels = historyData
      .map((item) => formatDate(item.updatedAt))
      .reverse();
    const values = historyData
      .map((item) => Number(item.newPrice))
      .reverse();

    const commodityLabels = {
      '24.00': 'Gold 24K',
      '22.00': 'Gold 22K',
      '18.00': 'Gold 18K',
      '14.00': 'Gold 14K',
      '10.00': 'Gold 9K',
      'silver': 'Silver',
      'diamond': 'Diamond',
      'gst': 'GST',
    };

    const isPercent = commodity === 'gst';
    const label = commodityLabels[commodity] || commodity;

    chartInstanceRef.current = new window.Chart(ctx, {
      type: 'line',
      data: {
        labels,
        datasets: [
          {
            label,
            data: values,
            borderColor: '#1e88e5',
            backgroundColor: 'rgba(30, 136, 229, 0.15)',
            tension: 0.25,
            fill: true,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          y: {
            ticks: {
              callback: (value) => (isPercent ? `${value}%` : `₹${value}`),
            },
          },
        },
        plugins: {
          tooltip: {
            callbacks: {
              label: (context) => {
                const val = context.parsed.y;
                return isPercent
                  ? `${context.dataset.label}: ${val}%`
                  : `${context.dataset.label}: ₹${val}`;
              },
            },
          },
        },
      },
    });

    return () => {
      if (chartInstanceRef.current) {
        chartInstanceRef.current.destroy();
        chartInstanceRef.current = null;
      }
    };
  }, [historyData, chartLoaded, showTable, commodity]);

  const fetchHistory = async () => {
    setLoading(true);
    const params = new URLSearchParams();

    if (showCustomRange && fromDate) {
      params.set('from', fromDate);
      if (toDate) {
        params.set('to', toDate);
      }
    } else if (!showCustomRange && period) {
      params.set('period', period);
    }

    try {
      const url = `${API_BASE}/rate-history/${encodeURIComponent(commodity)}?${params.toString()}`;
      const res = await fetch(url, { credentials: 'include' });
      if (!res.ok) {
        throw new Error('Failed to fetch rate history.');
      }
      const data = await res.json();
      setHistoryData(data || []);
    } catch (err) {
      console.error(err);
      setHistoryData([]);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (isoString) => {
    const date = new Date(isoString);
    if (Number.isNaN(date.getTime())) return isoString;
    return date.toLocaleString('en-IN', {
      dateStyle: 'medium',
      timeStyle: 'short',
    });
  };

  const handleToggleCustomRange = () => {
    setShowCustomRange(!showCustomRange);
    setFromDate('');
    setToDate('');
  };

  return (
    <div className="price-history-page font-sans">
      <header className="history-header">
        <h1 className="history-title">
          <i className="fas fa-chart-line"></i> Price History
        </h1>
        <button onClick={() => onSwitchPage('set-price')} className="btn-back">
          <i className="fas fa-arrow-left"></i> Back
        </button>
      </header>

      <main className="history-container">
        <div className="history-card">
          <div className="controls-section">
            <div className="control-group">
              <label>Commodity</label>
              <select
                className="history-select"
                value={commodity}
                onChange={(e) => setCommodity(e.target.value)}
              >
                <option value="24.00">Gold 24K</option>
                <option value="22.00">Gold 22K</option>
                <option value="18.00">Gold 18K</option>
                <option value="14.00">Gold 14K</option>
                <option value="10.00">Gold 9K</option>
                <option value="silver">Silver</option>
                <option value="diamond">Diamond</option>
                <option value="gst">GST</option>
              </select>
            </div>

            <div className="control-group">
              <label>Period</label>
              <select
                className="history-select"
                value={period}
                disabled={showCustomRange}
                onChange={(e) => setPeriod(e.target.value)}
              >
                <option value="5d">Last 5 days</option>
                <option value="3m">Last 3 months</option>
                <option value="6m">Last 6 months</option>
                <option value="1y">Last 1 year</option>
                <option value="5y">Last 5 years</option>
              </select>
            </div>

            <div className="btn-group-actions">
              <button
                className={`btn-control ${showCustomRange ? 'active' : ''}`}
                onClick={handleToggleCustomRange}
              >
                {showCustomRange ? 'Use Standard Periods' : 'Select Date Range'}
              </button>

              <button
                className="btn-control"
                onClick={() => setShowTable(!showTable)}
                disabled={historyData.length === 0}
              >
                {showTable ? 'Show Chart' : 'Show Stats Table'}
              </button>
            </div>
          </div>

          {showCustomRange && (
            <div className="date-range-fields">
              <div className="control-group">
                <label>From</label>
                <input
                  type="date"
                  className="history-input-date"
                  value={fromDate}
                  onChange={(e) => setFromDate(e.target.value)}
                />
              </div>
              <div className="control-group">
                <label>To</label>
                <input
                  type="date"
                  className="history-input-date"
                  value={toDate}
                  onChange={(e) => setToDate(e.target.value)}
                />
              </div>
            </div>
          )}

          {loading ? (
            <div className="history-loading">Loading history data...</div>
          ) : historyData.length === 0 ? (
            <div className="history-empty">No history data available for this selection.</div>
          ) : showTable ? (
            <div className="table-wrapper" style={{ marginTop: '20px' }}>
              <table className="stats-table">
                <thead>
                  <tr>
                    <th>Date/Time</th>
                    <th>Old Price</th>
                    <th>New Price</th>
                  </tr>
                </thead>
                <tbody>
                  {historyData.map((item, idx) => {
                    const isPercent = commodity === 'gst';
                    const formatVal = (val) =>
                      val === null || val === undefined || isNaN(Number(val))
                        ? '-'
                        : isPercent
                        ? `${Number(val)}%`
                        : `₹${Number(val).toLocaleString('en-IN')}`;

                    return (
                      <tr key={item.id || idx} className="hover:bg-gray-50 transition-colors">
                        <td className="font-semibold">{formatDate(item.updatedAt)}</td>
                        <td>{formatVal(item.oldPrice)}</td>
                        <td className="font-bold text-blue">{formatVal(item.newPrice)}</td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="chart-wrapper">
              <canvas ref={canvasRef}></canvas>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}

export default PriceHistory;
