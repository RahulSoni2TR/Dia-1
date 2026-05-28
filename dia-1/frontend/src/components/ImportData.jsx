import React, { useRef, useState } from 'react';
import './ImportData.css';

const API_BASE = import.meta.env.DEV ? 'http://localhost:8080' : '';

const categories = [
  { id: '1', name: 'Diamond' },
  { id: '2', name: 'Open Setting' },
  { id: '3', name: 'Plain Gold' },
  { id: '4', name: 'Vilandi' },
  { id: '5', name: 'Jadtar' }
];

function ImportData({ onSwitchPage }) {
  const [category, setCategory] = useState('');
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [modalMessage, setModalMessage] = useState('');
  const fileInputRef = useRef(null);

  const openModal = (message) => {
    setModalMessage(message);
  };

  const closeModal = () => {
    setModalMessage('');
  };

  const handleImport = async () => {
    if (!category || !file) {
      openModal('Please select a category and upload a file.');
      return;
    }

    const formData = new FormData();
    formData.append('file', file);
    formData.append('category', category);

    setLoading(true);
    try {
      const response = await fetch(`${API_BASE}/uploadFile`, {
        method: 'POST',
        body: formData,
        credentials: 'include'
      });

      const responseText = await response.text();
      if (!response.ok) {
        throw new Error(responseText || `HTTP error! status: ${response.status}`);
      }

      openModal(responseText || 'Data imported successfully');
      setFile(null);
      if (fileInputRef.current) fileInputRef.current.value = '';
    } catch (error) {
      openModal(`Import failed: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="import-data-page">
      <header className="header">
        <div className="logo">Product<span>Manager</span></div>
        <div className="logout-container">
          <button className="logout-btn" onClick={() => onSwitchPage('login')}>
            <i className="fas fa-sign-out-alt"></i> Logout
          </button>
        </div>
      </header>

      <main className="import-data-main">
        <section className="import-panel">
          <h1><i className="fas fa-file-import"></i> Import Data</h1>
          <p>Upload product data files to keep your catalog updated.</p>

          <label className="import-field">
            <span><i className="fas fa-tags"></i> Select Category:</span>
            <select value={category} onChange={(event) => setCategory(event.target.value)}>
              <option value="">Select Category</option>
              {categories.map(item => (
                <option key={item.id} value={item.id}>{item.name}</option>
              ))}
            </select>
          </label>

          <label className="import-field">
            <span><i className="fas fa-upload"></i> Upload File:</span>
            <input
              ref={fileInputRef}
              type="file"
              accept=".csv,.xlsx"
              onChange={(event) => setFile(event.target.files?.[0] || null)}
            />
          </label>

          <div className="import-actions">
            <button type="button" onClick={handleImport} disabled={loading}>
              <i className="fas fa-cloud-upload-alt"></i> Import Data
            </button>
            <a href={`${API_BASE}/files/sample.xlsx`} download className="download-sample-btn">
              <i className="fas fa-file-download"></i> Download Sample
            </a>
          </div>

          <button className="import-back-button" type="button" onClick={() => onSwitchPage('home')}>
            <i className="fas fa-arrow-left"></i> Back to Home
          </button>
        </section>

        <section className="import-notes">
          <p><strong>Notes:</strong></p>
          <ul>
            <li>DO NOT KEEP BLANK VALUES IN EXCEL SHEET. THIS MIGHT NOT IMPORT THE DATA.</li>
            <li>MAKE SURE TO KEEP A UNIQUE DESIGN NO FOR EVERY PRODUCT. IF THE DESIGN NO IS DUPLICATE, THEN SYSTEM WILL CREATE A NEW/ALTERNATE DESIGN NO FOR THAT PRODUCT.</li>
            <li>IMPORT DATA FOLLOWS A SPECIFIC EXCEL TEMPLATE, PLEASE USE THE SAMPLE EXCEL TEMPLATE TO ADD THE DATA. USE "DOWNLOAD SAMPLE" BUTTON TO GET THE SAMPLE.</li>
          </ul>
        </section>
      </main>

      {loading && (
        <div className="import-loading-overlay">
          <div className="import-loading-content">
            <div className="import-spinner"></div>
            <p>Import in progress...</p>
          </div>
        </div>
      )}

      {modalMessage && (
        <div className="import-modal-overlay">
          <div className="import-modal-content">
            <p>{modalMessage}</p>
            <button onClick={closeModal}>OK</button>
          </div>
        </div>
      )}

      <footer className="import-page-footer">
        <span>&copy; 2026 Jewellery Store. All rights reserved.</span>
      </footer>
    </div>
  );
}

export default ImportData;
