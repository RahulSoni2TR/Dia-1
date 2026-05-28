import React, { useEffect, useState } from 'react';
import pdfMake from "pdfmake/build/pdfmake.js";
import "pdfmake/build/vfs_fonts.js";
import './Modal.css';

const API_BASE = import.meta.env.DEV ? 'http://localhost:8080' : '';

function EstimateSnapshot({ onSwitchPage }) {
  const [estimate, setEstimate] = useState(null);
  const [returnPage, setReturnPage] = useState('enquiry-log');

  useEffect(() => {
    const storedEstimate = sessionStorage.getItem('estimateFromLog');
    const storedReturnPage = sessionStorage.getItem('returnToPage');
    
    if (storedEstimate) {
      try {
        const parsed = JSON.parse(storedEstimate);
        setEstimate(parsed);
      } catch (e) {
        console.error('Error parsing estimate:', e);
      }
    }
    
    if (storedReturnPage) {
      // Extract page name from URL if it's a full URL
      if (storedReturnPage.includes('enquiry')) {
        setReturnPage('enquiry-log');
      } else if (storedReturnPage.includes('sales')) {
        setReturnPage('sales-log');
      }
    }
  }, []);

  const handleBack = () => {
    sessionStorage.removeItem('estimateFromLog');
    sessionStorage.removeItem('returnToPage');
    onSwitchPage(returnPage);
  };

  const formatCurrency = (value) => `₹${Number(value || 0).toLocaleString('en-IN')}`;
  const formatNumber = (value) => Number(value || 0).toLocaleString('en-IN');
  const formatQty = (line) => {
    const qty = Number(line.qty || 0);
    if (qty <= 0) return '';
    return `${qty.toFixed(3)}${getUnit(line.description || '')}`;
  };
  const formatRate = (value) => Number(value || 0) > 0 ? Number(value).toFixed(2) : '';

  const handleDownloadPdf = () => {
    if (!estimate) return;

    const lines = (estimate.lines || []).filter(line => Number(line.amount || 0) > 0);
    const fileDesignNo = estimate.designNo || estimate.orderId || 'estimate';
    const formattedDate = estimate.clientTimestamp
      ? new Date(estimate.clientTimestamp).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' })
      : 'N/A';

    const body = [
      [
        { text: 'Description', style: 'tableHeader' },
        { text: 'Qty', style: 'tableHeader' },
        { text: 'Rate', style: 'tableHeader' },
        { text: 'Amount', style: 'tableHeader' }
      ],
      ...lines.map(line => [
        { text: line.description || '-', style: 'tableCell' },
        { text: formatQty(line), style: 'tableCell' },
        { text: formatRate(line.rate), style: 'tableCell' },
        { text: formatCurrency(line.amount), style: 'tableCell' }
      ])
    ];

    if (Number(estimate.totals?.gst || 0) > 0) {
      body.push([
        { text: 'GST', colSpan: 3, alignment: 'right', style: 'total' },
        {},
        {},
        { text: formatCurrency(estimate.totals.gst), style: 'total' }
      ]);
    }

    body.push([
      { text: 'Total', colSpan: 3, alignment: 'right', style: 'grandTotal' },
      {},
      {},
      { text: formatCurrency(estimate.totals?.grandTotal), style: 'grandTotal' }
    ]);

    const docDef = {
      pageSize: 'A4',
      pageMargins: [40, 40, 40, 40],
      content: [
        { text: `Estimate for ${estimate.item || ''}`, style: 'header', alignment: 'center' },
        { text: `Design No: ${estimate.orderId || ''}/${estimate.designNo || ''}`, alignment: 'center', margin: [0, 4, 0, 12] },
        {
          columns: [
            { text: `Customer: ${estimate.customerName || 'N/A'}` },
            { text: `Date: ${formattedDate}`, alignment: 'right' }
          ],
          margin: [0, 0, 0, 12]
        },
        {
          table: {
            headerRows: 1,
            widths: ['*', 'auto', 'auto', 'auto'],
            body
          },
          layout: 'lightHorizontalLines'
        }
      ],
      styles: {
        header: { fontSize: 18, bold: true, margin: [0, 0, 0, 8] },
        tableHeader: { bold: true, fontSize: 11, color: 'black' },
        tableCell: { fontSize: 10, color: 'black' },
        total: { bold: true, fontSize: 11, color: 'black' },
        grandTotal: { bold: true, fontSize: 12, color: 'black' }
      }
    };

    pdfMake.createPdf(docDef).download(`Estimate_${fileDesignNo}_${Date.now()}.pdf`);
  };

  if (!estimate) {
    return (
      <div className="modal-overlay">
        <div className="modal-content">
          <button className="modal-close-btn" onClick={handleBack}>&times;</button>
          <h2>Estimate Not Found</h2>
          <p>No estimate data available.</p>
          <button className="action-button secondary" onClick={handleBack}>
            Back
          </button>
        </div>
      </div>
    );
  }

  const getUnit = (desc) => {
    const d = desc.toLowerCase();
    if (d.includes('gold') || d.includes('labour') || d.includes('pearls')) return ' gm';
    if (d.includes('beads') || d.includes('diamonds') || d.includes('stones') || d.includes('vilandi') || d.includes('ss pearls')) return ' ct';
    return '';
  };

  return (
    <div className="modal-overlay">
      <div className="estimate-snapshot-modal-content">
        <button className="modal-close-btn" onClick={handleBack}>&times;</button>
        
        <div style={{ padding: '20px' }}>
          <h2 style={{ marginBottom: '20px', borderBottom: '2px solid #ddd', paddingBottom: '10px' }}>
            Estimate Snapshot
          </h2>

          {/* Estimate Details */}
          <div style={{ marginBottom: '20px' }}>
            <h3>Estimate Information</h3>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <tbody>
                <tr>
                  <td style={{ padding: '8px', borderBottom: '1px solid #eee', fontWeight: 'bold', width: '30%' }}>Item:</td>
                  <td style={{ padding: '8px', borderBottom: '1px solid #eee' }}>{estimate.item || '-'}</td>
                </tr>
                <tr>
                  <td style={{ padding: '8px', borderBottom: '1px solid #eee', fontWeight: 'bold' }}>Design No:</td>
                  <td style={{ padding: '8px', borderBottom: '1px solid #eee' }}>{estimate.designNo || '-'}</td>
                </tr>
                <tr>
                  <td style={{ padding: '8px', borderBottom: '1px solid #eee', fontWeight: 'bold' }}>Customer Name:</td>
                  <td style={{ padding: '8px', borderBottom: '1px solid #eee' }}>{estimate.customerName || '-'}</td>
                </tr>
                <tr>
                  <td style={{ padding: '8px', borderBottom: '1px solid #eee', fontWeight: 'bold' }}>Date:</td>
                  <td style={{ padding: '8px', borderBottom: '1px solid #eee' }}>
                    {estimate.clientTimestamp ? new Date(estimate.clientTimestamp).toLocaleString() : '-'}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          {/* Items Table */}
          {estimate.lines && estimate.lines.length > 0 && (
            <div style={{ marginBottom: '20px' }}>
              <h3>Estimated Items</h3>
              <table style={{ width: '100%', borderCollapse: 'collapse', border: '1px solid #ddd' }}>
                <thead style={{ backgroundColor: '#f5f5f5' }}>
                  <tr>
                    <th style={{ padding: '10px', textAlign: 'left', borderBottom: '2px solid #ddd' }}>Description</th>
                    <th style={{ padding: '10px', textAlign: 'right', borderBottom: '2px solid #ddd' }}>Quantity</th>
                    <th style={{ padding: '10px', textAlign: 'right', borderBottom: '2px solid #ddd' }}>Rate (per unit)</th>
                    <th style={{ padding: '10px', textAlign: 'right', borderBottom: '2px solid #ddd' }}>Amount</th>
                  </tr>
                </thead>
                <tbody>
                  {estimate.lines.map((line, idx) => (
                    <tr key={idx}>
                      <td style={{ padding: '10px', borderBottom: '1px solid #eee' }}>{line.description || '-'}</td>
                      <td style={{ padding: '10px', borderBottom: '1px solid #eee', textAlign: 'right' }}>
                        {line.qty > 0 ? `${line.qty.toFixed(3)}${getUnit(line.description)}` : '-'}
                      </td>
                      <td style={{ padding: '10px', borderBottom: '1px solid #eee', textAlign: 'right' }}>
                        {line.rate > 0 ? line.rate.toFixed(2) : '-'}
                      </td>
                      <td style={{ padding: '10px', borderBottom: '1px solid #eee', fontWeight: 'bold', textAlign: 'right' }}>
                        {line.amount ? line.amount.toLocaleString('en-IN') : '0'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {/* Total Section */}
          {estimate.totals && (
            <div style={{ marginBottom: '20px', padding: '15px', backgroundColor: '#f0f8ff', borderRadius: '5px' }}>
              <table style={{ width: '100%' }}>
                <tbody>
                  <tr>
                    <td style={{ padding: '4px', fontSize: '14px' }}>Subtotal (Excl. GST):</td>
                    <td style={{ padding: '4px', textAlign: 'right', fontSize: '14px' }}>
                      ₹{estimate.totals.noGst?.toLocaleString('en-IN')}
                    </td>
                  </tr>
                  <tr>
                    <td style={{ padding: '4px', fontSize: '14px' }}>GST:</td>
                    <td style={{ padding: '4px', textAlign: 'right', fontSize: '14px' }}>
                      ₹{estimate.totals.gst?.toLocaleString('en-IN')}
                    </td>
                  </tr>
                  <tr style={{ borderTop: '1px solid #ccc' }}>
                    <td style={{ padding: '8px 4px', fontWeight: 'bold', fontSize: '16px' }}>Grand Total:</td>
                    <td style={{ padding: '8px 4px', textAlign: 'right', fontWeight: 'bold', fontSize: '18px', color: '#2c3e50' }}>
                      ₹{estimate.totals.grandTotal?.toLocaleString('en-IN')}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          )}

          {/* Notes */}
          {estimate.notes && (
            <div style={{ marginBottom: '20px' }}>
              <h3>Notes</h3>
              <div style={{ padding: '10px', backgroundColor: '#fffbea', border: '1px solid #f0ad4e', borderRadius: '4px' }}>
                {estimate.notes}
              </div>
            </div>
          )}

          <div style={{ marginTop: '20px', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '12px', flexWrap: 'wrap' }}>
            <button className="action-button" onClick={handleDownloadPdf}>
              <i className="fas fa-download"></i> Download PDF
            </button>
            <button className="action-button secondary" onClick={handleBack}>
              <i className="fas fa-arrow-left"></i> Back to Log
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default EstimateSnapshot;
