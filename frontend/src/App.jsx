import React, { useState, useEffect } from 'react';
import { 
  Plus, Search, Copy, Check, ExternalLink, Calendar, 
  Trash2, Edit2, BarChart2, CheckCircle2, AlertTriangle, 
  X, Globe, ArrowLeft, MousePointer, Link, Clock
} from 'lucide-react';
import { 
  ResponsiveContainer, AreaChart, Area, XAxis, YAxis, Tooltip, CartesianGrid, 
  PieChart, Pie, Cell, BarChart, Bar
} from 'recharts';

const API_BASE = import.meta.env.VITE_API_BASE || "http://localhost:8080";

// Chart Color Palette
const COLORS = ['#6366f1', '#8b5cf6', '#a78bfa', '#10b981', '#3b82f6', '#fbbf24', '#ef4444', '#14b8a6', '#6b7280'];

export default function App() {
  // App views: 'dashboard' | 'analytics'
  const [activeView, setActiveView] = useState('dashboard');
  
  // Dashboard State
  const [links, setLinks] = useState([]);
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  
  // Summary Stats State
  const [summary, setSummary] = useState({
    totalLinks: 0,
    totalClicks: 0,
    activeLinks: 0,
    expiredLinks: 0
  });

  // Analytics View State
  const [analyticsLinkId, setAnalyticsLinkId] = useState(null);
  const [analyticsLinkDetails, setAnalyticsLinkDetails] = useState(null);
  const [analyticsData, setAnalyticsData] = useState(null);
  const [loadingAnalytics, setLoadingAnalytics] = useState(false);

  // Modals state
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [editingLink, setEditingLink] = useState(null);
  const [copiedId, setCopiedId] = useState(null);

  // Form State
  const [createForm, setCreateForm] = useState({
    title: '',
    fullUrl: '',
    customAlias: '',
    expiryDate: ''
  });

  const [editForm, setEditForm] = useState({
    id: '',
    title: '',
    customAlias: '',
    expiryDate: '',
    enabled: true
  });

  // Toast State
  const [toasts, setToasts] = useState([]);

  // Fetch initial summary & links
  useEffect(() => {
    fetchSummary();
    fetchLinks(search, page);
  }, [page]);

  // Handle Search Input changes
  const handleSearchChange = (e) => {
    const val = e.target.value;
    setSearch(val);
    setPage(0);
    fetchLinks(val, 0);
  };

  const showToast = (message, type = 'success') => {
    const id = Date.now();
    setToasts(prev => [...prev, { id, message, type }]);
    setTimeout(() => {
      setToasts(prev => prev.filter(t => t.id !== id));
    }, 4000);
  };

  const fetchSummary = async () => {
    try {
      const res = await fetch(`${API_BASE}/api/analytics/summary`);
      if (res.ok) {
        const data = await res.json();
        setSummary(data);
      }
    } catch (e) {
      console.error("Failed to fetch dashboard summary", e);
    }
  };

  const fetchLinks = async (searchQuery = '', pageNum = 0) => {
    try {
      const url = `${API_BASE}/api/links?search=${encodeURIComponent(searchQuery)}&page=${pageNum}&size=8&sort=createdAt,desc`;
      const res = await fetch(url);
      if (res.ok) {
        const data = await res.json();
        setLinks(data.content || []);
        setTotalPages(data.totalPages || 0);
        setTotalElements(data.totalElements || 0);
      }
    } catch (e) {
      showToast("Error loading links list", "error");
      console.error("Failed to fetch links", e);
    }
  };

  const copyToClipboard = (text, id) => {
    navigator.clipboard.writeText(text).then(() => {
      setCopiedId(id);
      showToast("Short link copied to clipboard!");
      setTimeout(() => setCopiedId(null), 2000);
    }).catch(err => {
      showToast("Failed to copy link", "error");
    });
  };

  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    if (!createForm.fullUrl) {
      showToast("Original URL is required", "warning");
      return;
    }

    try {
      const body = {
        title: createForm.title || null,
        fullUrl: createForm.fullUrl,
        customAlias: createForm.customAlias || null,
        expiryDate: createForm.expiryDate ? new Date(createForm.expiryDate).toISOString() : null
      };

      const res = await fetch(`${API_BASE}/api/links`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
      });

      if (res.ok) {
        showToast("Short URL created successfully!");
        setShowCreateModal(false);
        setCreateForm({ title: '', fullUrl: '', customAlias: '', expiryDate: '' });
        fetchSummary();
        fetchLinks(search, page);
      } else {
        const err = await res.json();
        showToast(err.message || "Failed to create short link", "error");
      }
    } catch (error) {
      showToast("Server network connection error", "error");
    }
  };

  const handleEditOpen = (link) => {
    setEditingLink(link);
    setEditForm({
      id: link.id,
      title: link.title || '',
      customAlias: link.shortCode || '',
      expiryDate: link.expiryDate ? link.expiryDate.substring(0, 16) : '',
      enabled: link.enabled
    });
    setShowEditModal(true);
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    try {
      const body = {
        title: editForm.title || null,
        customAlias: editForm.customAlias || null,
        expiryDate: editForm.expiryDate ? new Date(editForm.expiryDate).toISOString() : null,
        enabled: editForm.enabled
      };

      const res = await fetch(`${API_BASE}/api/links/${editForm.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
      });

      if (res.ok) {
        showToast("Link updated successfully!");
        setShowEditModal(false);
        fetchSummary();
        fetchLinks(search, page);
      } else {
        const err = await res.json();
        showToast(err.message || "Failed to update short link", "error");
      }
    } catch (error) {
      showToast("Server network connection error", "error");
    }
  };

  const handleToggleStatus = async (link) => {
    try {
      const body = {
        enabled: !link.enabled
      };
      const res = await fetch(`${API_BASE}/api/links/${link.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
      });

      if (res.ok) {
        showToast(link.enabled ? "Short link disabled" : "Short link enabled");
        fetchSummary();
        fetchLinks(search, page);
      } else {
        showToast("Failed to toggle link status", "error");
      }
    } catch (e) {
      showToast("Server network connection error", "error");
    }
  };

  const handleDeleteLink = async (id) => {
    if (!window.confirm("Are you sure you want to delete this short link? Visits stats will be preserved, but the redirection will stop.")) {
      return;
    }

    try {
      const res = await fetch(`${API_BASE}/api/links/${id}`, {
        method: 'DELETE'
      });

      if (res.ok) {
        showToast("Link deleted successfully (soft delete)");
        fetchSummary();
        fetchLinks(search, page);
      } else {
        showToast("Failed to delete short link", "error");
      }
    } catch (error) {
      showToast("Server network connection error", "error");
    }
  };

  const handleOpenAnalytics = async (link) => {
    setLoadingAnalytics(true);
    setActiveView('analytics');
    setAnalyticsLinkId(link.id);
    setAnalyticsLinkDetails(link);
    
    try {
      const res = await fetch(`${API_BASE}/api/links/${link.id}/analytics`);
      if (res.ok) {
        const data = await res.json();
        setAnalyticsData(data);
      } else {
        showToast("Failed to retrieve link analytics", "error");
      }
    } catch (error) {
      showToast("Error connecting to analytics server", "error");
      console.error(error);
    } finally {
      setLoadingAnalytics(false);
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return "-";
    const date = new Date(dateStr);
    return date.toLocaleDateString(undefined, { 
      year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' 
    });
  };

  const getStatusBadge = (link) => {
    if (!link.enabled) {
      return <span className="badge badge-disabled">Disabled</span>;
    }
    if (link.expiryDate && new Date(link.expiryDate) < new Date()) {
      return <span className="badge badge-expired">Expired</span>;
    }
    return <span className="badge badge-active">Active</span>;
  };

  return (
    <div className="app-container">
      {/* Toast Notification HUD */}
      <div className="toast-container">
        {toasts.map(toast => (
          <div key={toast.id} className={`toast ${toast.type}`}>
            <span className="toast-message">{toast.message}</span>
          </div>
        ))}
      </div>

      {/* Main Header */}
      <header className="app-header">
        <div className="brand-section">
          <div className="brand-logo">
            <Link size={24} color="#ffffff" strokeWidth={2.5} />
          </div>
          <div>
            <h1 className="brand-title">URL_SHORTNER</h1>
            <p className="brand-subtitle">AI-Powered URL Shortener Dashboard</p>
          </div>
        </div>
        
        {activeView === 'dashboard' && (
          <button className="btn btn-primary" onClick={() => setShowCreateModal(true)}>
            <Plus size={18} />
            Create Short Link
          </button>
        )}
      </header>

      {activeView === 'dashboard' ? (
        <>
          {/* Summary Dashboard Grid */}
          <section className="stats-grid">
            <div className="stat-card total">
              <div className="stat-icon">
                <Link size={20} />
              </div>
              <div className="stat-info">
                <span className="stat-value">{summary.totalLinks}</span>
                <span className="stat-label">Total Links</span>
              </div>
            </div>
            
            <div className="stat-card clicks">
              <div className="stat-icon">
                <MousePointer size={20} />
              </div>
              <div className="stat-info">
                <span className="stat-value">{summary.totalClicks}</span>
                <span className="stat-label">Total Clicks</span>
              </div>
            </div>

            <div className="stat-card active">
              <div className="stat-icon">
                <CheckCircle2 size={20} />
              </div>
              <div className="stat-info">
                <span className="stat-value">{summary.activeLinks}</span>
                <span className="stat-label">Active Links</span>
              </div>
            </div>

            <div className="stat-card expired">
              <div className="stat-icon">
                <Clock size={20} />
              </div>
              <div className="stat-info">
                <span className="stat-value">{summary.expiredLinks}</span>
                <span className="stat-label">Expired Links</span>
              </div>
            </div>
          </section>

          {/* Table Controls (Search & Filters) */}
          <div className="control-bar">
            <div className="search-container">
              <Search className="search-icon" />
              <input 
                type="text" 
                placeholder="Search short links by title or original URL..." 
                className="search-input"
                value={search}
                onChange={handleSearchChange}
              />
            </div>
          </div>

          {/* Links Table */}
          <div className="table-container">
            {links.length > 0 ? (
              <table className="links-table">
                <thead>
                  <tr>
                    <th>Title / Original URL</th>
                    <th>Short URL</th>
                    <th>Status</th>
                    <th>Clicks</th>
                    <th>Created Date</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {links.map(link => (
                    <tr key={link.id}>
                      <td>
                        <div className="link-title-col">
                          <span className="link-title" title={link.title}>{link.title || "Untitled Link"}</span>
                          <span className="link-original" title={link.fullUrl}>{link.fullUrl}</span>
                        </div>
                      </td>
                      <td>
                        <div className="link-short-wrapper">
                          <a href={link.shortUrl} target="_blank" rel="noopener noreferrer" className="link-short-text">
                            {link.shortCode}
                          </a>
                          <button className="copy-btn" onClick={() => copyToClipboard(link.shortUrl, link.id)}>
                            {copiedId === link.id ? <Check size={14} color="#10b981" /> : <Copy size={14} />}
                          </button>
                        </div>
                      </td>
                      <td>
                        {getStatusBadge(link)}
                      </td>
                      <td>
                        <div className="clicks-col">
                          <MousePointer className="clicks-icon" />
                          <span>{link.clickCount}</span>
                        </div>
                      </td>
                      <td>
                        <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                          {formatDate(link.createdAt)}
                        </span>
                      </td>
                      <td>
                        <div className="actions-cell">
                          <button 
                            className="action-btn toggle" 
                            title={link.enabled ? "Disable link" : "Enable link"}
                            onClick={() => handleToggleStatus(link)}
                          >
                            <Clock size={15} />
                          </button>
                          <button 
                            className="action-btn edit" 
                            title="Edit details"
                            onClick={() => handleEditOpen(link)}
                          >
                            <Edit2 size={15} />
                          </button>

                          <button 
                            className="action-btn delete" 
                            title="Delete link"
                            onClick={() => handleDeleteLink(link.id)}
                          >
                            <Trash2 size={15} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <div className="empty-state">
                <Link className="empty-state-icon" />
                <h3 className="empty-state-title">No Links Found</h3>
                <p className="empty-state-text">Create your first shortened marketing link using the button above to begin campaigns.</p>
              </div>
            )}

            {/* Pagination Controls */}
            {totalPages > 1 && (
              <div className="pagination">
                <div className="pagination-info">
                  Showing page <strong>{page + 1}</strong> of <strong>{totalPages}</strong> ({totalElements} total links)
                </div>
                <div className="pagination-controls">
                  <button 
                    className="btn btn-secondary" 
                    disabled={page === 0}
                    onClick={() => setPage(prev => Math.max(0, prev - 1))}
                  >
                    Previous
                  </button>
                  <button 
                    className="btn btn-secondary" 
                    disabled={page >= totalPages - 1}
                    onClick={() => setPage(prev => Math.min(totalPages - 1, prev + 1))}
                  >
                    Next
                  </button>
                </div>
              </div>
            )}
          </div>
        </>
      ) : (
        /* Analytics Dashboard View */
        <div>
          <div className="analytics-header-row">
            <button className="analytics-back-btn" onClick={() => { setActiveView('dashboard'); setAnalyticsData(null); fetchSummary(); fetchLinks(search, page); }}>
              <ArrowLeft size={16} />
              Back to Dashboard
            </button>
            <div className="analytics-title-area" style={{ textAlign: 'right' }}>
              <h2>Analytics details for &ldquo;{analyticsLinkDetails?.title}&rdquo;</h2>
              <p>{analyticsLinkDetails?.fullUrl}</p>
            </div>
          </div>

          {loadingAnalytics ? (
            <div className="empty-state" style={{ minHeight: '350px' }}>
              <Clock className="empty-state-icon" style={{ animation: 'spin 2s linear infinite' }} />
              <h3 className="empty-state-title">Analyzing Visitors</h3>
              <p className="empty-state-text">Aggregating geolocation data and traffic counts...</p>
            </div>
          ) : analyticsData && analyticsData.totalClicks > 0 ? (
            <>
              {/* Analytics Individual Metrics Card */}
              <section className="stats-grid">
                <div className="stat-card clicks" style={{ gridColumn: 'span 2' }}>
                  <div className="stat-icon">
                    <MousePointer size={22} />
                  </div>
                  <div className="stat-info">
                    <span className="stat-value">{analyticsData.totalClicks}</span>
                    <span className="stat-label">Total Visits Collected</span>
                  </div>
                </div>
                <div className="stat-card total" style={{ gridColumn: 'span 2' }}>
                  <div className="stat-icon">
                    <Clock size={22} />
                  </div>
                  <div className="stat-info">
                    <span className="stat-value">{formatDate(analyticsLinkDetails?.createdAt)}</span>
                    <span className="stat-label">Date Launched</span>
                  </div>
                </div>
              </section>

              {/* Charts Grid */}
              <section className="analytics-grid">
                
                {/* 1. Daily Clicks Chart */}
                <div className="chart-card" style={{ gridColumn: 'span 2' }}>
                  <h3 className="chart-title">
                    <Clock className="chart-title-icon" /> Daily Clicks History
                  </h3>
                  <div className="chart-wrapper">
                    <ResponsiveContainer width="100%" height={300}>
                      <AreaChart data={analyticsData.dailyClicks} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                        <defs>
                          <linearGradient id="colorClicks" x1="0" y1="0" x2="0" y2="1">
                            <stop offset="5%" stopColor="#6366f1" stopOpacity={0.4}/>
                            <stop offset="95%" stopColor="#6366f1" stopOpacity={0}/>
                          </linearGradient>
                        </defs>
                        <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
                        <XAxis dataKey="name" stroke="#9ca3af" fontSize={12} tickLine={false} />
                        <YAxis stroke="#9ca3af" fontSize={12} tickLine={false} />
                        <Tooltip 
                          contentStyle={{ backgroundColor: '#111827', borderColor: 'rgba(255,255,255,0.1)', color: '#f3f4f6' }}
                          labelStyle={{ color: '#9ca3af', fontWeight: 'bold' }}
                        />
                        <Area type="monotone" dataKey="value" stroke="#6366f1" strokeWidth={2.5} fillOpacity={1} fill="url(#colorClicks)" name="Clicks" />
                      </AreaChart>
                    </ResponsiveContainer>
                  </div>
                </div>

                {/* 2. Top Referrers */}
                <div className="chart-card">
                  <h3 className="chart-title">
                    <Globe className="chart-title-icon" /> Traffic Referrers
                  </h3>
                  <div style={{ flex: 1, overflowY: 'auto' }}>
                    <table className="dist-table">
                      <tbody>
                        {analyticsData.topReferrers.map((ref, idx) => {
                          const maxCount = Math.max(...analyticsData.topReferrers.map(r => r.value));
                          const percent = maxCount > 0 ? (ref.value / maxCount) * 100 : 0;
                          return (
                            <tr key={idx}>
                              <td>
                                <div className="dist-name">{ref.name}</div>
                                <div className="dist-bar-wrapper">
                                  <div className="dist-bar" style={{ width: `${percent}%` }} />
                                </div>
                              </td>
                              <td className="dist-count">{ref.value}</td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </table>
                  </div>
                </div>

                {/* 3. Country Distribution */}
                <div className="chart-card">
                  <h3 className="chart-title">
                    <Globe className="chart-title-icon" /> Geolocation (Top Countries)
                  </h3>
                  <div className="chart-wrapper">
                    <ResponsiveContainer width="100%" height={260}>
                      <BarChart data={analyticsData.countryDistribution} margin={{ top: 10, right: 0, left: -20, bottom: 0 }}>
                        <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
                        <XAxis dataKey="name" stroke="#9ca3af" fontSize={11} tickLine={false} />
                        <YAxis stroke="#9ca3af" fontSize={11} tickLine={false} />
                        <Tooltip contentStyle={{ backgroundColor: '#111827', borderColor: 'rgba(255,255,255,0.1)', color: '#f3f4f6' }} />
                        <Bar dataKey="value" radius={[4, 4, 0, 0]} name="Visits">
                          {analyticsData.countryDistribution.map((entry, index) => (
                            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                          ))}
                        </Bar>
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                </div>

                {/* 4. Browser Distribution */}
                <div className="chart-card">
                  <h3 className="chart-title">
                    <MousePointer className="chart-title-icon" /> Browsers
                  </h3>
                  <div className="chart-wrapper" style={{ display: 'flex', gap: '2rem' }}>
                    <div style={{ width: '60%' }}>
                      <ResponsiveContainer width="100%" height={240}>
                        <PieChart>
                          <Pie
                            data={analyticsData.browserDistribution}
                            cx="50%"
                            cy="50%"
                            innerRadius={50}
                            outerRadius={80}
                            paddingAngle={4}
                            dataKey="value"
                          >
                            {analyticsData.browserDistribution.map((entry, index) => (
                              <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                            ))}
                          </Pie>
                          <Tooltip contentStyle={{ backgroundColor: '#111827', borderColor: 'rgba(255,255,255,0.1)', color: '#f3f4f6' }} />
                        </PieChart>
                      </ResponsiveContainer>
                    </div>
                    <div style={{ width: '40%', display: 'flex', flexDirection: 'column', justifyContent: 'center', gap: '0.5rem' }}>
                      {analyticsData.browserDistribution.map((item, idx) => (
                        <div key={idx} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.8rem' }}>
                          <span style={{ display: 'inline-block', width: '10px', height: '10px', borderRadius: '50%', backgroundColor: COLORS[idx % COLORS.length] }} />
                          <span style={{ fontWeight: 600 }}>{item.name}</span>
                          <span style={{ color: 'var(--text-muted)' }}>({item.value})</span>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>

                {/* 5. Device Distribution */}
                <div className="chart-card">
                  <h3 className="chart-title">
                    <Globe className="chart-title-icon" /> Device Usage
                  </h3>
                  <div className="chart-wrapper" style={{ display: 'flex', gap: '2rem' }}>
                    <div style={{ width: '60%' }}>
                      <ResponsiveContainer width="100%" height={240}>
                        <PieChart>
                          <Pie
                            data={analyticsData.deviceDistribution}
                            cx="50%"
                            cy="50%"
                            innerRadius={50}
                            outerRadius={80}
                            paddingAngle={4}
                            dataKey="value"
                          >
                            {analyticsData.deviceDistribution.map((entry, index) => (
                              <Cell key={`cell-${index}`} fill={COLORS[(index + 3) % COLORS.length]} />
                            ))}
                          </Pie>
                          <Tooltip contentStyle={{ backgroundColor: '#111827', borderColor: 'rgba(255,255,255,0.1)', color: '#f3f4f6' }} />
                        </PieChart>
                      </ResponsiveContainer>
                    </div>
                    <div style={{ width: '40%', display: 'flex', flexDirection: 'column', justifyContent: 'center', gap: '0.5rem' }}>
                      {analyticsData.deviceDistribution.map((item, idx) => (
                        <div key={idx} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.8rem' }}>
                          <span style={{ display: 'inline-block', width: '10px', height: '10px', borderRadius: '50%', backgroundColor: COLORS[(idx + 3) % COLORS.length] }} />
                          <span style={{ fontWeight: 600 }}>{item.name}</span>
                          <span style={{ color: 'var(--text-muted)' }}>({item.value})</span>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>

                {/* 6. OS Distribution */}
                <div className="chart-card" style={{ gridColumn: 'span 2' }}>
                  <h3 className="chart-title">
                    <Laptop className="chart-title-icon" style={{ width: 18, height: 18, marginRight: 6 }} /> Operating Systems
                  </h3>
                  <div className="chart-wrapper">
                    <ResponsiveContainer width="100%" height={260}>
                      <BarChart data={analyticsData.osDistribution} margin={{ top: 10, right: 0, left: -20, bottom: 0 }}>
                        <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
                        <XAxis dataKey="name" stroke="#9ca3af" fontSize={11} tickLine={false} />
                        <YAxis stroke="#9ca3af" fontSize={11} tickLine={false} />
                        <Tooltip contentStyle={{ backgroundColor: '#111827', borderColor: 'rgba(255,255,255,0.1)', color: '#f3f4f6' }} />
                        <Bar dataKey="value" radius={[4, 4, 0, 0]} name="Users">
                          {analyticsData.osDistribution.map((entry, index) => (
                            <Cell key={`cell-${index}`} fill={COLORS[(index + 5) % COLORS.length]} />
                          ))}
                        </Bar>
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                </div>

              </section>
            </>
          ) : (
            <div className="empty-state" style={{ minHeight: '400px' }}>
              <Clock className="empty-state-icon" />
              <h3 className="empty-state-title">No visits yet</h3>
              <p className="empty-state-text">This link hasn&apos;t been visited yet. Share the link <a href={analyticsLinkDetails?.shortUrl} target="_blank" rel="noreferrer" style={{ color: '#a5b4fc' }}>{analyticsLinkDetails?.shortUrl}</a> with your audience to begin gathering analytics data.</p>
            </div>
          )}
        </div>
      )}

      {/* CREATE LINK MODAL */}
      {showCreateModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h2 className="modal-title">
                <Plus size={18} color="#8b5cf6" />
                Create Short Link
              </h2>
              <button className="modal-close" onClick={() => setShowCreateModal(false)}>
                <X size={18} />
              </button>
            </div>
            <form onSubmit={handleCreateSubmit}>
              <div className="modal-body">
                <div className="form-group">
                  <label className="form-label">Title</label>
                  <input 
                    type="text" 
                    placeholder="E.g., Winter Campaign Promo" 
                    className="form-control"
                    value={createForm.title}
                    onChange={e => setCreateForm(prev => ({ ...prev, title: e.target.value }))}
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Original URL *</label>
                  <input 
                    type="url" 
                    required
                    placeholder="https://example.com/very-long-marketing-url" 
                    className="form-control"
                    value={createForm.fullUrl}
                    onChange={e => setCreateForm(prev => ({ ...prev, fullUrl: e.target.value }))}
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Custom Alias (Optional)</label>
                  <input 
                    type="text" 
                    placeholder="E.g., winter2026" 
                    className="form-control"
                    value={createForm.customAlias}
                    onChange={e => setCreateForm(prev => ({ ...prev, customAlias: e.target.value }))}
                  />
                  <small style={{ color: 'var(--text-dark)', fontSize: '0.75rem', marginTop: '4px', display: 'block' }}>
                    Alphanumeric, hyphens, and underscores only.
                  </small>
                </div>
                <div className="form-group">
                  <label className="form-label">Expiration Date (Optional)</label>
                  <input 
                    type="datetime-local" 
                    className="form-control"
                    value={createForm.expiryDate}
                    onChange={e => setCreateForm(prev => ({ ...prev, expiryDate: e.target.value }))}
                  />
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowCreateModal(false)}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary">
                  Generate Link
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* EDIT LINK MODAL */}
      {showEditModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h2 className="modal-title">
                <Edit2 size={18} color="#3b82f6" />
                Edit Link Details
              </h2>
              <button className="modal-close" onClick={() => setShowEditModal(false)}>
                <X size={18} />
              </button>
            </div>
            <form onSubmit={handleEditSubmit}>
              <div className="modal-body">
                <div className="form-group">
                  <label className="form-label">Original URL (Read-only)</label>
                  <input 
                    type="text" 
                    disabled
                    className="form-control"
                    value={editingLink?.fullUrl}
                    style={{ opacity: 0.6, cursor: 'not-allowed' }}
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Title</label>
                  <input 
                    type="text" 
                    className="form-control"
                    value={editForm.title}
                    onChange={e => setEditForm(prev => ({ ...prev, title: e.target.value }))}
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Custom Alias / Short Code</label>
                  <input 
                    type="text" 
                    className="form-control"
                    value={editForm.customAlias}
                    onChange={e => setEditForm(prev => ({ ...prev, customAlias: e.target.value }))}
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Expiration Date</label>
                  <input 
                    type="datetime-local" 
                    className="form-control"
                    value={editForm.expiryDate}
                    onChange={e => setEditForm(prev => ({ ...prev, expiryDate: e.target.value }))}
                  />
                </div>
                <div className="form-group" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: '1.5rem' }}>
                  <div>
                    <label className="form-label" style={{ marginBottom: 0 }}>Redirection Status</label>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                      {editForm.enabled ? "Link is active and redirecting" : "Link is paused and displays disabled warning"}
                    </span>
                  </div>
                  <label className="switch">
                    <input 
                      type="checkbox" 
                      checked={editForm.enabled}
                      onChange={e => setEditForm(prev => ({ ...prev, enabled: e.target.checked }))}
                    />
                    <span className="slider"></span>
                  </label>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowEditModal(false)}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary">
                  Save Changes
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
