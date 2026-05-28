import React, { useState, useEffect, useCallback } from 'react';
import './SalesLog.css';

const API_BASE = import.meta.env.DEV ? 'http://localhost:8080' : '';

const categoryMap = {
  "1": "Diamond",
  "2": "Open Setting",
  "3": "Plain Gold",
  "4": "Vilandi",
  "5": "Jadtar"
};

const subCategoriesMap = {
  "Jadtar": [
    { id: 6, name: "Jadtar Register" }, { id: 7, name: "Jadtar Halfsets" },
    { id: 8, name: "Jadtar Bangles / Bracelets" }, { id: 17, name: "Only Earrings" }
  ],
  "Vilandi": [
    { id: 9, name: "Vilandi Halfsets" }, { id: 10, name: "Vilandi Bangles / Bracelets" }
  ],
  "Diamond": [
    { id: 20, name: "Diamond Rings" }, { id: 19, name: "Diamond Earrings" },
    { id: 11, name: "Diamond Bangles / Bracelets" }, { id: 12, name: "Diamond Pendants / Pendant Sets" },
    { id: 13, name: "Diamond Halfsets" }
  ],
  "Open Setting": [
    { id: 14, name: "OS Halfsets" }, { id: 15, name: "OS Bangles / Bracelets" }
  ],
  "Plain Gold": [
    { id: 18, name: "Chains" }, { id: 16, name: "PG Bangles / Bracelets" }
  ]
};

const parseSnapshot = (snapshot) => {
  if (typeof snapshot !== 'string') {
    return snapshot || {};
  }

  try {
    let parsed = JSON.parse(snapshot);
    if (typeof parsed === 'string') parsed = JSON.parse(parsed);
    return parsed || {};
  } catch (e) {
    console.error("Snapshot parse error", e);
    return {};
  }
};

const hasStoredPrice = (value) => value !== null && value !== undefined && value !== '' && Number(value) !== 0;

const mergeLogPricesIntoProduct = (productSnapshot, logRow) => ({
  ...productSnapshot,
  price: hasStoredPrice(productSnapshot?.price) ? productSnapshot.price : (logRow.price ?? 0),
  priceWithFields: hasStoredPrice(productSnapshot?.priceWithFields)
    ? productSnapshot.priceWithFields
    : (logRow.priceWithFields ?? logRow.price ?? 0)
});

function SalesLog({ onSwitchPage }) {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  
  // Filters & Sorting
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [selectedSubCategory, setSelectedSubCategory] = useState('');
  const [sort, setSort] = useState({ field: 'createdAt', dir: 'desc' });

  const fetchSalesLogs = useCallback(async () => {
    setLoading(true);
    setError(null);
    const params = new URLSearchParams({
      page: String(currentPage),
      size: '10',
      sort: `${sort.field},${sort.dir}`
    });

    if (searchTerm) params.append('q', searchTerm.trim());
    if (selectedCategory) params.append('categoryIdStr', selectedCategory);
    if (selectedSubCategory) params.append('subCategoryIdStr', selectedSubCategory);

    try {
      const res = await fetch(`${API_BASE}/api/sales-logs?${params.toString()}`, { credentials: 'include' });
      
      const contentType = res.headers.get("content-type");
      if (!res.ok || !contentType || !contentType.includes("application/json")) {
        throw new Error('Failed to fetch sales logs. Please check if you are logged in.');
      }

      const data = await res.json();
      setLogs(data.content || []);
      setTotalPages(data.totalPages || 1);
    } catch (err) {
      console.error(err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [currentPage, searchTerm, selectedCategory, selectedSubCategory, sort]);

  useEffect(() => {
    fetchSalesLogs();
  }, [fetchSalesLogs]);

  const handleSort = (field) => {
    setSort(prev => ({
      field,
      dir: prev.field === field && prev.dir === 'asc' ? 'desc' : 'asc'
    }));
    setCurrentPage(0);
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this sale log?")) return;
    try {
      const res = await fetch(`${API_BASE}/api/sales-logs/${id}`, { method: 'DELETE', credentials: 'include' });
      if (res.ok) fetchSalesLogs();
    } catch (err) {
      alert("Delete failed");
    }
  };

  const resetFilters = () => {
    setSearchTerm('');
    setSelectedCategory('');
    setSelectedSubCategory('');
    setSort({ field: 'createdAt', dir: 'desc' });
    setCurrentPage(0);
  };

  const handleViewAction = (type, snapshot) => {
    const key = type === 'product' ? 'productFromLog' : 'estimateFromLog';
    const page = type === 'product' ? 'product-snapshot' : 'estimate-snapshot';

    sessionStorage.setItem(key, JSON.stringify(parseSnapshot(snapshot)));
    sessionStorage.setItem('returnToPage', 'sales-log');
    onSwitchPage(page);
  };

  return (
    <div className="sales-log-page font-sans">
      <header className="log-header">
        <h1 className="log-title">
          <i className="fas fa-box-open"></i> Sold Products Log
        </h1>
        <button onClick={() => onSwitchPage('home')} className="btn-back">
          <i className="fas fa-arrow-left"></i> Back
        </button>
      </header>

      <main className="log-container">
        <div className="log-card">
          {/* Filter Bar */}
          <div className="filter-section">
            <div className="filter-input-group">
              <input 
                type="text" 
                placeholder="Search by design no, order id, or customer..." 
                className="outline-none"
                value={searchTerm}
                onChange={(e) => { setSearchTerm(e.target.value); setCurrentPage(0); }}
              />
              <div className="bg-green-600 text-white px-4 h-full flex items-center"><i className="fas fa-search"></i></div>
            </div>

            <select 
              className="filter-select"
              value={selectedCategory}
              onChange={(e) => { setSelectedCategory(e.target.value); setSelectedSubCategory(''); setCurrentPage(0); }}
            >
              <option value="">Select Category</option>
              {Object.entries(categoryMap).map(([id, name]) => <option key={id} value={id}>{name}</option>)}
            </select>

            <select 
              className="filter-select"
              value={selectedSubCategory}
              disabled={!selectedCategory}
              onChange={(e) => { setSelectedSubCategory(e.target.value); setCurrentPage(0); }}
            >
              <option value="">Select Subcategory</option>
              {selectedCategory && subCategoriesMap[categoryMap[selectedCategory]]?.map(sub => (
                <option key={sub.id} value={sub.id}>{sub.name}</option>
              ))}
            </select>
          </div>

          <div className="reset-container">
            <button onClick={resetFilters} className="btn-reset">
              <i className="fas fa-undo"></i> Reset Filters
            </button>
          </div>

          {/* Data Table */}
          <div className="table-wrapper">
            <table className="sales-table">
              <thead>
                <tr>
                  <th className="col-index">#</th>
                  <th className="col-image">Image</th>
                  <th>Product Name</th>
                  <th className="col-design">Design No</th>
                  <th className="col-order">Order ID</th>
                  <th>Customer</th>
                  <th
                    className="col-date cursor-pointer group"
                    onClick={() => handleSort('createdAt')}
                  >
                    Created At
                    <i className={`ml-1 fas ${sort.field === 'createdAt' ? (sort.dir === 'asc' ? 'fa-sort-up' : 'fa-sort-down') : 'fa-sort text-gray-300'}`}></i>
                  </th>
                  <th className="col-actions">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {loading ? (
                  <tr><td colSpan="8" className="text-center py-10 text-gray-500">Loading sales logs...</td></tr>
                ) : error ? (
                  <tr><td colSpan="8" className="text-center py-10 text-red-500 font-medium">{error}</td></tr>
                ) : logs.length === 0 ? (
                  <tr><td colSpan="8" className="text-center py-10 text-gray-500">No records found</td></tr>
                ) : logs.map((log, idx) => {
                  const snapshot = mergeLogPricesIntoProduct(parseSnapshot(log.productSnapshot), log);

                  const displayImageUrl = log.imageUrl || snapshot?.imageUrl;
                  const finalImageUrl = displayImageUrl?.startsWith('/') ? `${API_BASE}${displayImageUrl}` : displayImageUrl;

                  return (
                    <tr key={log.id} className="hover:bg-gray-50 transition-colors">
                      <td className="col-index font-medium">{idx + 1 + currentPage * 10}</td>
                      <td>
                        {finalImageUrl ? (
                          <img src={finalImageUrl} className="product-thumbnail" alt="Product" />
                        ) : <span className="italic opacity-50">No image</span>}
                      </td>
                      <td className="font-semibold">{snapshot?.item || '-'}</td>
                      <td>{log.designNo || '-'}</td>
                      <td>{log.orderId || '-'}</td>
                      <td>{log.customerName || '-'}</td>
                      <td>{new Date(log.createdAt).toLocaleString()}</td>
                      <td>
                        <div className="action-btn-group">
                          <button onClick={() => handleViewAction('product', snapshot)} className="btn-view-prod">View Product</button>
                          <button onClick={() => handleViewAction('estimate', log.estimateSnapshot)} className="btn-view-est">View Estimate</button>
                          <button onClick={() => handleDelete(log.id)} className="btn-remove">Delete</button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          <div className="pagination-bar text-sm text-gray-600">
            <button disabled={currentPage === 0} onClick={() => setCurrentPage(prev => prev - 1)} className="btn-pagination btn-pagination-prev"><i className="fas fa-chevron-left mr-1"></i> Previous</button>
            <div className="font-medium bg-gray-100 px-3 py-1 rounded-full">Page {currentPage + 1} of {totalPages}</div>
            <button disabled={currentPage + 1 >= totalPages} onClick={() => setCurrentPage(prev => prev + 1)} className="btn-pagination btn-pagination-next">Next <i className="fas fa-chevron-right ml-1"></i></button>
          </div>
        </div>
      </main>
    </div>
  );
}

export default SalesLog;
