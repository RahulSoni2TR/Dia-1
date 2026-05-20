import { useState, useEffect } from 'react';

const API_BASE = import.meta.env.DEV ? 'http://localhost:8080' : '';

function ViewRates({ onSwitchPage }) {
  const [rates, setRates] = useState({});
  const [silverPrice, setSilverPrice] = useState('--');
  const [loading, setLoading] = useState(true);
  const [date, setDate] = useState('');

  useEffect(() => {
    const now = new Date();
    const dd = String(now.getDate()).padStart(2, '0');
    const mm = String(now.getMonth() + 1).padStart(2, '0');
    const yyyy = now.getFullYear();
    setDate(`${dd}/${mm}/${yyyy}`);
    
    loadRates();
  }, []);

  const loadRates = async () => {
    try {
      setLoading(true);
      const response = await fetch(`${API_BASE}/rates`, { credentials: 'include' });
      if (!response.ok) throw new Error('Failed to fetch rates');
      
      const data = await response.json();
      
      // Handle both direct array and wrapped response
      const ratesData = Array.isArray(data) ? data : (data.rates || []);
      
      const ratesMap = {};
      ratesData.forEach(rate => {
        if (rate.commodity && rate.price) {
          const key = rate.commodity.toString().toLowerCase();
          if (key === 'silver' || key === 'ag') {
            ratesMap['silver'] = rate.price;
          } else {
            ratesMap[rate.commodity] = rate.price;
          }
        }
      });
      
      setRates(ratesMap);
      setSilverPrice(`₹ ${formatPrice(ratesMap['silver'])}`);
    } catch (error) {
      console.error('Error loading rates:', error);
      setRates({});
      // Show friendly message if rates are unavailable
      setSilverPrice('₹ --');
    } finally {
      setLoading(false);
    }
  };

  const formatPrice = (value) => {
    if (value === null || value === undefined || Number.isNaN(Number(value))) {
      return '-';
    }
    return `${Number(value).toLocaleString('en-IN')}`;
  };

  const commodityRows = [
    { label: '24KT', key: '24.00', purity: '' },
    { label: '22KT', key: '22.00', purity: '' },
    { label: '18KT', key: '18.00', purity: '' },
    { label: '14KT', key: '14.00', purity: '' },
    { label: '9KT',  key: '10.00',  purity: '' }
  ];

  return (
    <div className="rates-card">
      <div className="card-header">
        <h1>Gold Rates</h1>
        <p className="subtitle">{date}</p>
      </div>

      {loading ? (
        <div className="loading">Loading rates...</div>
      ) : (
        <div className="gold-section">
          <div className="table-wrap">
            <table className="rates-table">
              <thead><tr></tr></thead>
              <tbody>
                {commodityRows.map((row) => (
                  <tr key={row.key}>
                    <td>{row.label}</td>
                    <td>{row.purity}</td>
                    <td className="price">₹ {formatPrice(rates[row.key])}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="silver-section">
            <div className="silver-box">
              <div>Silver</div>
              <div></div>
              <div className="price">{silverPrice}</div>
            </div>
          </div>
        </div>
      )}

      <p className="note">Rates are excluding GST.</p>
    </div>
  );
}

export default ViewRates;