import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { gsap } from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'
import {
  Activity, ArrowDownRight, ArrowRight, ArrowUpRight, Bell, Box, Building2,
  CalendarClock, Check, ChevronDown, ChevronLeft, ChevronRight, CircleHelp,
  ClipboardList, CloudUpload, Command, Download, FileClock, FileText, Filter,
  LayoutDashboard, LoaderCircle, LogOut, Menu, MoreHorizontal, Package,
  Plus, Search, Settings2, ShieldCheck, Sparkles, TrendingUp, Truck, Upload,
  Users, WandSparkles, X,
} from 'lucide-react'
import {
  Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis,
} from 'recharts'

gsap.registerPlugin(ScrollTrigger)

const API = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
const money = (paise = 0) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(Number(paise) / 100)
const quantity = (number) => Number(number || 0).toLocaleString('en-IN', { maximumFractionDigits: 3 })
const shortDate = (value) => value ? new Date(value).toLocaleDateString('en-IN', { day: '2-digit', month: 'short' }) : '—'

async function request(path, token, options = {}) {
  const headers = new Headers(options.headers || {})
  if (token) headers.set('Authorization', `Bearer ${token}`)
  if (options.body && !(options.body instanceof FormData)) headers.set('Content-Type', 'application/json')
  const response = await fetch(`${API}${path}`, { ...options, headers })
  if (response.status === 204) return null
  const raw = await response.text()
  let body = null
  try { body = raw ? JSON.parse(raw) : null } catch { body = raw }
  if (!response.ok) throw new Error(body?.message || body?.error || `Request failed (${response.status})`)
  return body
}

const navItems = [
  { id: 'overview', label: 'Overview', icon: LayoutDashboard },
  { id: 'inventory', label: 'Inventory', icon: Package, badge: 'inventory' },
  { id: 'sales', label: 'Sales & orders', icon: ClipboardList },
  { id: 'invoices', label: 'Invoice capture', icon: FileText, fresh: true },
  { id: 'forecasts', label: 'Forecasts', icon: TrendingUp },
]

function AuthScreen({ onAuth }) {
  const [mode, setMode] = useState('login')
  const [form, setForm] = useState({ fullName: '', email: '', password: '', businessName: '' })
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const change = (event) => setForm((previous) => ({ ...previous, [event.target.name]: event.target.value }))
  const submit = async (event) => {
    event.preventDefault(); setError(''); setBusy(true)
    try {
      const result = await request(`/api/auth/${mode === 'login' ? 'login' : 'register'}`, null, {
        method: 'POST', body: JSON.stringify(mode === 'login'
          ? { email: form.email, password: form.password }
          : form),
      })
      onAuth(result)
    } catch (exception) { setError(exception.message) } finally { setBusy(false) }
  }
  return <main className="auth-shell">
    <section className="auth-story">
      <a className="brand brand-light" href="#top"><span className="brand-mark"><Command size={19} /></span>stockpilot<span className="brand-dot">.</span></a>
      <div className="story-copy">
        <span className="eyebrow"><span className="live-dot" /> INVENTORY, WITH A LITTLE FORESIGHT</span>
        <h1>Know what’s moving.<br /><em>Before it’s gone.</em></h1>
        <p>One clear view of your stock, sales, and what to order next. Less guesswork for the people who keep business moving.</p>
        <div className="story-proof"><span className="proof-icon"><Sparkles size={16} /></span><span>Built for independent businesses<span className="proof-sub">Simple stock control. Smarter decisions.</span></span></div>
      </div>
      <div className="story-foot"><span>INVENTORY INTELLIGENCE, MADE HUMAN</span><span>01 — 04</span></div>
      <div className="story-orbit orbit-one" /><div className="story-orbit orbit-two" />
    </section>
    <section className="auth-side" id="top">
      <div className="auth-top"><span>Already tracking your stock?</span><button className="text-button" onClick={() => setMode(mode === 'login' ? 'register' : 'login')}>{mode === 'login' ? 'Create an account' : 'Sign in'} <ArrowRight size={14} /></button></div>
      <div className="auth-card">
        <div className="auth-icon"><Building2 size={20} /></div>
        <p className="eyebrow">{mode === 'login' ? 'WELCOME BACK' : 'LET’S GET YOU STARTED'}</p>
        <h2>{mode === 'login' ? 'Sign in to StockPilot' : 'Create your workspace'}</h2>
        <p className="auth-hint">{mode === 'login' ? 'Your business is right where you left it.' : 'A clearer view of your inventory starts here.'}</p>
        <form onSubmit={submit} className="auth-form">
          {mode === 'register' && <>
            <label>Your name<input name="fullName" value={form.fullName} onChange={change} placeholder="e.g. Priya Sharma" minLength="2" maxLength="80" required /></label>
            <label>Business name<input name="businessName" value={form.businessName} onChange={change} placeholder="e.g. Green Basket Market" minLength="2" maxLength="120" required /></label>
          </>}
          <label>Email address<input type="email" name="email" value={form.email} onChange={change} placeholder="you@yourbusiness.com" required /></label>
          <label>Password<div className="password-wrap"><input type={showPassword ? 'text' : 'password'} name="password" value={form.password} onChange={change} placeholder="At least 8 characters" minLength="8" maxLength="72" required /><button type="button" className="show-password" onClick={() => setShowPassword((value) => !value)}>{showPassword ? 'Hide' : 'Show'}</button></div></label>
          {error && <div className="form-error"><CircleHelp size={16} />{error}</div>}
          <button className="button button-dark auth-submit" disabled={busy}>{busy ? <LoaderCircle className="spin" size={17} /> : mode === 'login' ? 'Sign in' : 'Create account'} <ArrowRight size={16} /></button>
        </form>
        <p className="secure-note"><ShieldCheck size={15} /> Your business data stays private and secure.</p>
      </div>
      <div className="auth-bottom"><span>© 2026 StockPilot</span><span>Made for the ones who make it happen.</span></div>
    </section>
  </main>
}

function App() {
  const [token, setToken] = useState(() => localStorage.getItem('stockpilot.token'))
  const [profile, setProfile] = useState(() => { try { return JSON.parse(localStorage.getItem('stockpilot.profile')) } catch { return null } })
  const [page, setPage] = useState('overview')
  const [products, setProducts] = useState([])
  const [forecasts, setForecasts] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [toast, setToast] = useState('')
  const [query, setQuery] = useState('')
  const [modal, setModal] = useState('')
  const [mobileNav, setMobileNav] = useState(false)
  const headerRef = useRef(null)
  const contentRef = useRef(null)

  const acceptAuth = (result) => {
    localStorage.setItem('stockpilot.token', result.token)
    localStorage.setItem('stockpilot.profile', JSON.stringify(result))
    setToken(result.token); setProfile(result)
  }
  const signOut = () => {
    localStorage.removeItem('stockpilot.token'); localStorage.removeItem('stockpilot.profile')
    setToken(null); setProfile(null); setProducts([]); setForecasts([]); setPage('overview')
  }
  const loadData = useCallback(async () => {
    if (!token) return
    setLoading(true); setError('')
    try {
      const [productData, forecastData] = await Promise.all([
        request('/api/products', token), request('/api/forecasts', token).catch(() => []),
      ])
      setProducts(Array.isArray(productData) ? productData : [])
      setForecasts(Array.isArray(forecastData) ? forecastData : [])
    } catch (exception) {
      setError(exception.message)
      if (/401|403|expired|token/i.test(exception.message)) signOut()
    } finally { setLoading(false) }
  }, [token])

  useEffect(() => { if (token) loadData() }, [token, loadData])
  useEffect(() => {
    if (!token || !headerRef.current) return
    const ctx = gsap.context(() => {
      gsap.fromTo('.animate-in', { y: 18, opacity: 0 }, { y: 0, opacity: 1, duration: 0.65, stagger: 0.07, ease: 'power3.out' })
      gsap.fromTo('.reveal-line', { scaleX: 0, transformOrigin: 'left' }, { scaleX: 1, duration: 0.8, ease: 'power2.out', scrollTrigger: { trigger: '.reveal-line', start: 'top 90%' } })
    }, contentRef)
    return () => ctx.revert()
  }, [token, page, loading])

  const summary = useMemo(() => {
    const low = products.filter((product) => product.status === 'LOW_STOCK' || product.status === 'OUT_OF_STOCK' || Number(product.currentQuantity) <= Number(product.reorderLevel)).length
    const inventoryValue = products.reduce((sum, product) => sum + Number(product.currentQuantity || 0) * Number(product.costPricePaise || 0), 0)
    return { total: products.length, low, inventoryValue }
  }, [products])
  const filteredProducts = useMemo(() => products.filter((product) => `${product.name} ${product.sku} ${product.category || ''}`.toLowerCase().includes(query.toLowerCase())), [products, query])
  const needsAttention = useMemo(() => forecasts.filter((item) => item.lowStockAlert || Number(item.daysOfCover) < Number(item.leadTimeDays)), [forecasts])

  if (!token) return <AuthScreen onAuth={acceptAuth} />

  const pageTitle = navItems.find((item) => item.id === page)?.label || 'Overview'
  const navigate = (target) => { setPage(target); setMobileNav(false); setError('') }

  return <div className="app-shell">
    <aside className={`sidebar ${mobileNav ? 'sidebar-open' : ''}`}>
      <a className="brand" href="#home" onClick={(event) => { event.preventDefault(); navigate('overview') }}><span className="brand-mark"><Command size={19} /></span>stockpilot<span className="brand-dot">.</span></a>
      <div className="workspace-switch"><div className="workspace-avatar">{(profile?.businessName || 'S').slice(0, 1).toUpperCase()}</div><div className="workspace-label"><strong>{profile?.businessName || 'My business'}</strong><span>Workspace</span></div><ChevronDown size={15} /></div>
      <div className="nav-caption">WORKSPACE</div>
      <nav className="primary-nav">{navItems.map(({ id, label, icon: Icon, fresh }) => <button className={`nav-link ${page === id ? 'active' : ''}`} onClick={() => navigate(id)} key={id}><Icon size={18} strokeWidth={1.8} /><span>{label}</span>{fresh && <span className="new-tag">NEW</span>}{id === 'inventory' && summary.low > 0 && <span className="nav-count">{summary.low}</span>}</button>)}</nav>
      <div className="sidebar-bottom"><div className="sidebar-promo"><div className="promo-spark"><WandSparkles size={17} /></div><strong>Smarter stock starts here</strong><p>See what needs attention before it runs out.</p><button onClick={() => navigate('forecasts')}>Explore forecasts <ArrowRight size={14} /></button><div className="promo-orb" /></div><button className="nav-link muted-link"><Settings2 size={18} /><span>Settings</span></button><button className="nav-link muted-link"><CircleHelp size={18} /><span>Help & support</span></button><div className="user-menu"><div className="user-avatar">{(profile?.fullName || profile?.email || 'U').slice(0, 1).toUpperCase()}</div><div className="user-label"><strong>{profile?.fullName || 'Account'}</strong><span>{profile?.email}</span></div><button className="icon-button logout-button" title="Sign out" onClick={signOut}><LogOut size={17} /></button></div></div>
    </aside>
    {mobileNav && <button className="nav-scrim" aria-label="Close navigation" onClick={() => setMobileNav(false)} />}

    <main className="main-shell" ref={contentRef}>
      <header className="topbar" ref={headerRef}>
        <button className="icon-button mobile-menu" onClick={() => setMobileNav(true)}><Menu size={21} /></button>
        <div className="breadcrumb"><span>Workspace</span><ChevronRight size={14} /><strong>{pageTitle}</strong></div>
        <div className="top-actions"><div className="search-box"><Search size={16} /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search anything..." /><kbd>⌘ K</kbd></div><button className="icon-button notification-button" title="Notifications"><Bell size={18} /><i /></button><div className="top-avatar">{(profile?.fullName || 'U').slice(0, 1).toUpperCase()}</div></div>
      </header>
      <div className="page-content">
        {error && <div className="error-banner"><CircleHelp size={17} /><span>{error}</span><button className="icon-button" onClick={() => setError('')}><X size={16} /></button></div>}
        {page === 'overview' && <Overview products={products} forecasts={forecasts} needsAttention={needsAttention} summary={summary} loading={loading} onNavigate={navigate} onAdd={() => setModal('product')} />}
        {page === 'inventory' && <Inventory products={filteredProducts} loading={loading} query={query} setQuery={setQuery} onAdd={() => setModal('product')} onRefresh={loadData} />}
        {page === 'sales' && <Sales products={products} token={token} onDone={async (message) => { setToast(message); await loadData() }} />}
        {page === 'invoices' && <Invoices products={products} token={token} onDone={async (message) => { setToast(message); await loadData() }} />}
        {page === 'forecasts' && <Forecasts forecasts={forecasts} products={products} loading={loading} onRefresh={loadData} />}
      </div>
    </main>
    {modal === 'product' && <ProductModal token={token} onClose={() => setModal('')} onCreated={async () => { setModal(''); setToast('Product added to inventory'); await loadData() }} />}
    {toast && <div className="toast"><span className="toast-check"><Check size={14} /></span>{toast}<button className="icon-button" onClick={() => setToast('')}><X size={15} /></button></div>}
  </div>
}

function PageHeading({ eyebrow, title, description, action, onAction }) {
  return <div className="page-heading animate-in"><div><div className="eyebrow">{eyebrow}</div><h1>{title}</h1><p>{description}</p></div>{action && <button className="button button-dark" onClick={onAction}>{action === 'Add product' ? <Plus size={16} /> : <Upload size={16} />}{action}</button>}</div>
}

function Overview({ products, forecasts, needsAttention, summary, loading, onNavigate, onAdd }) {
  const chartData = useMemo(() => {
    return products.slice(0, 7).map((product) => ({ name: product.name.length > 12 ? `${product.name.slice(0, 11)}…` : product.name, onHand: Number(product.currentQuantity || 0), reorder: Number(product.reorderLevel || 0) }))
  }, [products])
  const inventoryRate = products.length ? Math.max(0, Math.round((products.length - summary.low) / products.length * 100)) : 0
  const alertItems = needsAttention.slice(0, 4)
  return <>
    <div className="welcome-row animate-in"><div><div className="eyebrow"><span className="live-dot" /> YOUR BUSINESS AT A GLANCE</div><h1>Good day, {profileFirstName()}<span className="wave">✳</span></h1><p>Here’s what’s happening with your inventory today.</p></div><div className="date-pill"><CalendarClock size={15} />{new Date().toLocaleDateString('en-IN', { weekday: 'long', day: 'numeric', month: 'short' })}</div></div>
    <section className="metric-grid">
      <MetricCard label="Total products" value={loading ? '—' : summary.total} foot="Active in your inventory" icon={Box} tone="mint" trend="Live" />
      <MetricCard label="Inventory value" value={loading ? '—' : money(summary.inventoryValue)} foot="Based on purchase cost" icon={Activity} tone="lavender" trend="Updated" />
      <MetricCard label="Needs attention" value={loading ? '—' : summary.low} foot="At or below reorder level" icon={Bell} tone="peach" trend={summary.low ? 'Review' : 'All clear'} alert={summary.low > 0} />
      <MetricCard label="Stock health" value={loading ? '—' : `${inventoryRate}%`} foot="Items above reorder level" icon={ShieldCheck} tone="blue" trend={summary.low ? 'Monitor' : 'Healthy'} />
    </section>
    <div className="overview-grid">
      <section className="panel chart-panel animate-in"><div className="panel-heading"><div><div className="eyebrow">INVENTORY PULSE</div><h2>Stock at a glance</h2></div><button className="select-pill">Top products <ChevronDown size={14} /></button></div><div className="chart-legend"><span><i className="legend-stock" /> On hand</span><span><i className="legend-sales" /> Reorder level</span></div><div className="chart-wrap">{chartData.length ? <ResponsiveContainer width="100%" height="100%"><AreaChart data={chartData} margin={{ top: 12, right: 5, left: -20, bottom: 0 }}><defs><linearGradient id="stockFill" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stopColor="#a9d5c2" stopOpacity={0.5} /><stop offset="95%" stopColor="#a9d5c2" stopOpacity={0.02} /></linearGradient><linearGradient id="salesFill" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stopColor="#c1b9ee" stopOpacity={0.36} /><stop offset="95%" stopColor="#c1b9ee" stopOpacity={0.01} /></linearGradient></defs><CartesianGrid vertical={false} stroke="#edf0eb" strokeDasharray="3 5" /><XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fill: '#929991', fontSize: 9 }} dy={10} /><YAxis axisLine={false} tickLine={false} tick={{ fill: '#929991', fontSize: 11 }} /><Tooltip contentStyle={{ borderRadius: 12, border: '1px solid #edf0eb', boxShadow: '0 10px 30px #1a2b2012' }} /><Area type="monotone" dataKey="onHand" name="On hand" stroke="#4d9477" strokeWidth={2.5} fill="url(#stockFill)" /><Area type="monotone" dataKey="reorder" name="Reorder level" stroke="#a59ad9" strokeWidth={2} fill="url(#salesFill)" /></AreaChart></ResponsiveContainer> : <EmptyState icon={Package} title="No stock to chart yet" text="Add products to compare their current quantities with reorder levels." action="Add first product" onAction={onAdd} />}</div><div className="chart-foot"><span><span className="chart-foot-dot" /> Live quantities from your catalogue</span><button className="inline-link" onClick={() => onNavigate('inventory')}>View inventory <ArrowRight size={14} /></button></div></section>
      <section className="panel attention-panel animate-in"><div className="panel-heading"><div><div className="eyebrow">A LITTLE HEADS-UP</div><h2>Needs your attention</h2></div><span className={`attention-count ${alertItems.length ? '' : 'good-count'}`}>{alertItems.length || '✓'}</span></div>{loading ? <Loading /> : alertItems.length ? <div className="attention-list">{alertItems.map((item, index) => <div className="attention-item" key={item.productId}><div className={`attention-icon attention-${index % 3}`}><Package size={17} /></div><div className="attention-copy"><strong>{item.name}</strong><span>{quantity(item.currentQuantity)} {item.unit?.toLowerCase()} left <i>·</i> {item.daysOfCover == null ? 'Low stock' : `${quantity(item.daysOfCover)} days cover`}</span></div><button className="icon-button chevron-quiet" onClick={() => onNavigate('forecasts')}><ChevronRight size={18} /></button></div>)}<button className="attention-link" onClick={() => onNavigate('forecasts')}>See all recommendations <ArrowRight size={14} /></button></div> : <div className="empty-attention"><span className="empty-check"><Check size={17} /></span><strong>You’re in a good spot</strong><p>We’ll flag items that need a reorder.</p><button className="inline-link" onClick={() => onNavigate('inventory')}>Review inventory <ArrowRight size={14} /></button></div>}</section>
    </div>
    <section className="panel inventory-panel animate-in"><div className="panel-heading"><div><div className="eyebrow">YOUR SHELVES, DIGITALLY</div><h2>Inventory snapshot</h2></div><button className="inline-link" onClick={() => onNavigate('inventory')}>All products <ArrowRight size={14} /></button></div>{loading ? <Loading /> : products.length ? <div className="table-scroll"><table><thead><tr><th>PRODUCT</th><th>SKU</th><th>ON HAND</th><th>REORDER AT</th><th>STATUS</th><th>UNIT COST</th></tr></thead><tbody>{products.slice(0, 5).map((product) => { const low = Number(product.currentQuantity) <= Number(product.reorderLevel); return <tr key={product.id}><td><div className="product-cell"><div className="product-thumb"><Package size={16} /></div><div><strong>{product.name}</strong><span>{product.category || 'Uncategorised'}</span></div></div></td><td className="mono-text">{product.sku}</td><td><strong>{quantity(product.currentQuantity)}</strong> <span className="unit-muted">{product.unit?.toLowerCase()}</span></td><td>{quantity(product.reorderLevel)} {product.unit?.toLowerCase()}</td><td><span className={`status-pill ${low ? 'status-low' : 'status-good'}`}><i />{low ? 'Low stock' : 'In stock'}</span></td><td>{money(product.costPricePaise)}</td></tr>})}</tbody></table></div> : <EmptyState icon={Package} title="Your inventory is ready for its first product" text="Add a product to start tracking stock levels, purchase cost, and reorder points." action="Add first product" onAction={() => onNavigate('inventory')} />}</section>
    <div className="bottom-note animate-in"><span><Sparkles size={15} /> A good inventory day starts with a little visibility.</span><span>Updated just now <span className="update-dot" /></span></div>
  </>
}

function profileFirstName() { const name = JSON.parse(localStorage.getItem('stockpilot.profile') || '{}')?.fullName || 'there'; return name.split(' ')[0] }

function MetricCard({ label, value, foot, icon: Icon, tone, trend, alert }) {
  return <article className="metric-card animate-in"><div className="metric-top"><span>{label}</span><span className={`metric-icon ${tone}`}><Icon size={17} strokeWidth={1.8} /></span></div><div className="metric-value">{value}</div><div className="metric-bottom"><span>{foot}</span><span className={`trend-tag ${alert ? 'trend-warn' : ''}`}>{alert && <ArrowDownRight size={12} />}{trend}</span></div></article>
}

function Inventory({ products, loading, query, setQuery, onAdd, onRefresh }) {
  const [filter, setFilter] = useState('all')
  const list = products.filter((p) => filter === 'all' || (filter === 'low' ? Number(p.currentQuantity) <= Number(p.reorderLevel) : Number(p.currentQuantity) > Number(p.reorderLevel)))
  return <><PageHeading eyebrow="YOUR CATALOGUE" title="Inventory" description="A living view of what you have, what’s moving, and what needs a top-up." action="Add product" onAction={onAdd} /><div className="toolbar animate-in"><div className="toolbar-search"><Search size={16} /><input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Find a product or SKU..." /></div><div className="filter-tabs"><button className={filter === 'all' ? 'filter-active' : ''} onClick={() => setFilter('all')}>All items <span>{products.length}</span></button><button className={filter === 'low' ? 'filter-active' : ''} onClick={() => setFilter('low')}>Low stock</button><button className={filter === 'healthy' ? 'filter-active' : ''} onClick={() => setFilter('healthy')}>Healthy</button></div><button className="icon-button refresh-button" onClick={onRefresh}><Download size={16} /></button></div><section className="panel inventory-panel animate-in">{loading ? <Loading /> : list.length ? <div className="table-scroll"><table><thead><tr><th>PRODUCT</th><th>SKU</th><th>ON HAND</th><th>REORDER AT</th><th>LEAD TIME</th><th>STATUS</th><th>UNIT COST</th></tr></thead><tbody>{list.map((product) => { const low = Number(product.currentQuantity) <= Number(product.reorderLevel); return <tr key={product.id}><td><div className="product-cell"><div className="product-thumb"><Package size={16} /></div><div><strong>{product.name}</strong><span>{product.category || 'Uncategorised'}</span></div></div></td><td className="mono-text">{product.sku}</td><td><strong>{quantity(product.currentQuantity)}</strong> <span className="unit-muted">{product.unit?.toLowerCase()}</span></td><td>{quantity(product.reorderLevel)} {product.unit?.toLowerCase()}</td><td>{product.leadTimeDays} days</td><td><span className={`status-pill ${low ? 'status-low' : 'status-good'}`}><i />{low ? 'Low stock' : 'In stock'}</span></td><td>{money(product.costPricePaise)}</td></tr>})}</tbody></table></div> : <EmptyState icon={Package} title="No products found" text="Try another search, or add the first product to your catalogue." action="Add product" onAction={onAdd} />}</section><div className="bottom-note"><span>{list.length} products shown</span><span>Stock quantities update as sales and purchases are recorded.</span></div></>
}

function Sales({ products, token, onDone }) {
  const [lines, setLines] = useState([{ productId: '', quantity: '1' }])
  const [busy, setBusy] = useState(false); const [error, setError] = useState('')
  const updateLine = (index, key, value) => setLines((prev) => prev.map((line, i) => i === index ? { ...line, [key]: value } : line))
  const addLine = () => setLines((prev) => [...prev, { productId: '', quantity: '1' }])
  const removeLine = (index) => setLines((prev) => prev.filter((_, i) => i !== index))
  const total = lines.reduce((sum, line) => { const product = products.find((p) => p.id === line.productId); return sum + Number(line.quantity || 0) * (product?.sellingPricePaise || 0) }, 0)
  const submit = async (event) => {
    event.preventDefault(); setError(''); setBusy(true)
    try {
      await request('/api/sales', token, { method: 'POST', headers: { 'Idempotency-Key': crypto.randomUUID() }, body: JSON.stringify({ items: lines.map(({ productId, quantity: qty }) => ({ productId, quantity: Number(qty) })) }) })
      onDone('Sale recorded and stock updated')
      setLines([{ productId: '', quantity: '1' }])
    } catch (exception) { setError(exception.message) } finally { setBusy(false) }
  }
  return <><PageHeading eyebrow="RING IT THROUGH" title="Sales & orders" description="Record a sale in a few clicks. Stock levels take care of themselves." /><div className="form-layout"><section className="panel form-panel"><div className="panel-heading"><div><div className="eyebrow">NEW TRANSACTION</div><h2>Record a sale</h2></div><span className="sale-step">01 <i>/</i> 01</span></div><form onSubmit={submit}><div className="sale-lines-heading"><span>PRODUCTS</span><span>QUANTITY</span><span /></div>{lines.map((line, index) => { const product = products.find((p) => p.id === line.productId); return <div className="sale-line" key={index}><select value={line.productId} onChange={(e) => updateLine(index, 'productId', e.target.value)} required><option value="">Choose a product</option>{products.map((productItem) => <option key={productItem.id} value={productItem.id}>{productItem.name} · {quantity(productItem.currentQuantity)} {productItem.unit?.toLowerCase()} available</option>)}</select><input type="number" min="0.001" step="0.001" max={product?.currentQuantity} value={line.quantity} onChange={(e) => updateLine(index, 'quantity', e.target.value)} required /><button className="icon-button remove-line" type="button" onClick={() => removeLine(index)} disabled={lines.length === 1}><X size={15} /></button></div>})}<button type="button" className="add-line-button" onClick={addLine}><Plus size={15} /> Add another item</button>{error && <div className="form-error"><CircleHelp size={16} />{error}</div>}<div className="sale-total"><span>Sale total <small>Estimated from listed selling prices</small></span><strong>{money(total)}</strong></div><button className="button button-dark form-submit" disabled={busy || !products.length}>{busy ? <LoaderCircle className="spin" size={16} /> : <Check size={16} />} Confirm sale</button></form></section><aside className="panel side-note-panel"><span className="note-icon note-green"><ShieldCheck size={18} /></span><div className="eyebrow">SAFE BY DESIGN</div><h3>Stock stays in sync.</h3><p>Each sale is recorded once. We check available stock before we confirm, so your count won’t dip below zero.</p><div className="side-note-stat"><span><Package size={15} /> Products in catalogue</span><strong>{products.length}</strong></div><div className="side-note-stat"><span><Activity size={15} /> Available to sell</span><strong>{quantity(products.reduce((sum, p) => sum + Number(p.currentQuantity || 0), 0))} units</strong></div></aside></div></>
}

function Invoices({ products, token, onDone }) {
  const [file, setFile] = useState(null); const [ocr, setOcr] = useState(null); const [reference, setReference] = useState(''); const [lines, setLines] = useState([{ productId: '', quantity: '' }]); const [busy, setBusy] = useState(false); const [error, setError] = useState(''); const fileRef = useRef(null)
  const upload = async () => {
    if (!file) { setError('Choose an invoice file first.'); return }
    setBusy(true); setError('')
    try { const form = new FormData(); form.append('file', file); const result = await request('/api/invoices/ocr', token, { method: 'POST', body: form }); setOcr(result); setReference(result.suggestedInvoiceNumber || `INV-${new Date().toISOString().slice(0, 10)}`) }
    catch (exception) { setError(exception.message) } finally { setBusy(false) }
  }
  const confirm = async (event) => {
    event.preventDefault(); setBusy(true); setError('')
    try { await request('/api/invoices/receive', token, { method: 'POST', body: JSON.stringify({ reference, lines: lines.map((line) => ({ productId: line.productId, quantity: Number(line.quantity) })) }) }); onDone('Invoice confirmed — stock received') ; setOcr(null); setFile(null); setLines([{ productId: '', quantity: '' }]) }
    catch (exception) { setError(exception.message) } finally { setBusy(false) }
  }
  const setLine = (index, key, value) => setLines((prev) => prev.map((line, i) => i === index ? { ...line, [key]: value } : line))
  return <><PageHeading eyebrow="FROM PAPER TO ON HAND" title="Invoice capture" description="Pull the details from a supplier invoice, review them, then receive stock." /><div className="invoice-layout"><section className="panel invoice-main"><div className="invoice-stepper"><span className="step-active"><i>01</i> Upload</span><span className={ocr ? 'step-active' : ''}><i>02</i> Review</span><span><i>03</i> Receive stock</span></div>{!ocr ? <div className="upload-area" onClick={() => fileRef.current?.click()} onDragOver={(e) => e.preventDefault()} onDrop={(e) => { e.preventDefault(); setFile(e.dataTransfer.files[0] || null) }}><input ref={fileRef} type="file" accept=".jpg,.jpeg,.png,.pdf,image/jpeg,image/png,application/pdf" hidden onChange={(e) => setFile(e.target.files[0] || null)} /><span className="upload-icon"><CloudUpload size={22} /></span><strong>{file ? file.name : 'Drop your invoice here'}</strong><span>{file ? `${(file.size / 1024 / 1024).toFixed(2)} MB · Ready to scan` : 'or browse files from your device'}</span><small>JPG, PNG or PDF · max 5 MB</small><button type="button" className="button button-outline" onClick={(e) => { e.stopPropagation(); fileRef.current?.click() }}><Upload size={15} /> Choose file</button></div> : <><div className="ocr-success"><span className="success-check"><Check size={16} /></span><div><strong>Invoice scanned</strong><span>{ocr.fileName} · Check the text before receiving stock</span></div><button className="inline-link" onClick={() => { setOcr(null); setError('') }}>Scan another</button></div><div className="ocr-review"><div className="review-heading"><div><div className="eyebrow">EXTRACTED TEXT</div><strong>Review the original reading</strong></div><span className="review-tag"><Sparkles size={13} /> OCR draft</span></div><textarea value={ocr.extractedText} readOnly rows={8} /><div className="review-warning"><CircleHelp size={15} />{ocr.reviewRequired}</div></div></>}{!ocr && <button type="button" className="button button-dark upload-submit" onClick={upload} disabled={busy || !file}>{busy ? <LoaderCircle className="spin" size={16} /> : <Sparkles size={16} />} Read invoice</button>}{ocr && <form onSubmit={confirm} className="receive-form"><div className="eyebrow">REVIEW & RECEIVE</div><label>Invoice reference<input value={reference} onChange={(e) => setReference(e.target.value)} maxLength="80" required /></label><div className="receive-lines-head"><span>LINK TO PRODUCT</span><span>QUANTITY TO ADD</span><span /></div>{lines.map((line, index) => <div className="receive-line" key={index}><select value={line.productId} onChange={(e) => setLine(index, 'productId', e.target.value)} required><option value="">Match an inventory product</option>{products.map((product) => <option key={product.id} value={product.id}>{product.name} · {product.sku}</option>)}</select><input type="number" min="0.001" step="0.001" value={line.quantity} onChange={(e) => setLine(index, 'quantity', e.target.value)} placeholder="0" required /><button type="button" className="icon-button remove-line" disabled={lines.length === 1} onClick={() => setLines((prev) => prev.filter((_, i) => i !== index))}><X size={15} /></button></div>)}<button type="button" className="add-line-button" onClick={() => setLines((prev) => [...prev, { productId: '', quantity: '' }])}><Plus size={15} /> Add another product</button>{error && <div className="form-error"><CircleHelp size={16} />{error}</div>}<button className="button button-dark form-submit" disabled={busy}>{busy ? <LoaderCircle className="spin" size={16} /> : <Package size={16} />} Confirm & add stock</button></form>}{error && !ocr && <div className="form-error upload-error"><CircleHelp size={16} />{error}</div>}</section><aside className="panel side-note-panel invoice-note"><span className="note-icon note-lavender"><WandSparkles size={18} /></span><div className="eyebrow">A HUMAN IN THE LOOP</div><h3>Paper in.<br />Confidence out.</h3><p>OCR helps you get started. You review the details and match products before we touch your stock count.</p><div className="privacy-note"><ShieldCheck size={16} /><span>Stock changes only after you confirm.</span></div></aside></div></>
}

function Forecasts({ forecasts, products, loading, onRefresh }) {
  const items = forecasts.length ? forecasts : []
  const enough = items.filter((item) => item.enoughHistory).length
  return <><PageHeading eyebrow="AHEAD OF THE CURVE" title="Forecasts" description="A practical heads-up for the products you may need to reorder." action="Refresh" onAction={onRefresh} /><section className="forecast-banner animate-in"><div className="forecast-art"><span /><span /><span /><TrendingUp size={23} /></div><div><div className="eyebrow">14-DAY SALES SIGNAL</div><h2>Order with a little more confidence.</h2><p>We look at recent sales velocity and supplier lead times to help you get ahead of a stock-out.</p></div><div className="forecast-availability"><strong>{enough} / {items.length}</strong><span>with enough sales history</span></div></section><div className="forecast-stats"><MetricCard label="Products tracked" value={loading ? '—' : items.length || products.length} foot="Across your active catalogue" icon={Package} tone="mint" trend="Live" /><MetricCard label="Reorder signals" value={loading ? '—' : items.filter((item) => item.lowStockAlert).length} foot="Below target stock level" icon={Bell} tone="peach" trend="Review" alert={items.some((item) => item.lowStockAlert)} /><MetricCard label="Sales history" value={loading ? '—' : `${enough} ready`} foot="Needs 7+ days of activity" icon={FileClock} tone="lavender" trend="14 days" /><MetricCard label="Supplier lead time" value={loading ? '—' : `${Math.round(products.reduce((s, p) => s + Number(p.leadTimeDays || 0), 0) / Math.max(products.length, 1))} days`} foot="Average across products" icon={Truck} tone="blue" trend="Planning" /></div><section className="panel inventory-panel forecast-table animate-in"><div className="panel-heading"><div><div className="eyebrow">THE REORDER RADAR</div><h2>Product recommendations</h2></div><span className="forecast-updated"><span className="update-dot" /> Fresh from your sales</span></div>{loading ? <Loading /> : items.length ? <div className="table-scroll"><table><thead><tr><th>PRODUCT</th><th>ON HAND</th><th>DAILY SALES</th><th>DAYS COVER</th><th>STOCK-OUT ETA</th><th>RECOMMENDATION</th></tr></thead><tbody>{items.map((item) => <tr key={item.productId}><td><div className="product-cell"><div className="product-thumb"><TrendingUp size={16} /></div><div><strong>{item.name}</strong><span>{item.sku}</span></div></div></td><td>{quantity(item.currentQuantity)} <span className="unit-muted">{item.unit?.toLowerCase()}</span></td><td>{item.enoughHistory ? `${quantity(item.averageDailySales)} / day` : <span className="muted-cell">Building history</span>}</td><td><span className={`cover-pill ${Number(item.daysOfCover) <= Number(item.leadTimeDays) ? 'cover-risk' : ''}`}>{item.daysOfCover == null ? '—' : `${quantity(item.daysOfCover)} days`}</span></td><td>{item.predictedStockOutDate ? shortDate(item.predictedStockOutDate) : 'Not predicted'}</td><td><span className={`recommendation ${Number(item.recommendedQuantity) > 0 ? 'recommend-active' : ''}`}>{Number(item.recommendedQuantity) > 0 ? `Order ${quantity(item.recommendedQuantity)} ${item.unit?.toLowerCase()}` : item.enoughHistory ? 'No order needed' : 'More data needed'}</span></td></tr>)}</tbody></table></div> : <EmptyState icon={TrendingUp} title="Forecasts are warming up" text="Add products and record sales. Forecasts need recent sales history before they can make useful recommendations." action="Go to inventory" onAction={onRefresh} />}</section><p className="forecast-footnote"><Sparkles size={14} /> Recommendations are estimates based on the sales history available. Review supplier plans before ordering.</p></>
}

function ProductModal({ token, onClose, onCreated }) {
  const [form, setForm] = useState({ name: '', sku: '', category: '', unit: 'PCS', sellingPrice: '', costPrice: '', initialQuantity: '', reorderLevel: '', packSize: '1', leadTimeDays: '3' })
  const [busy, setBusy] = useState(false); const [error, setError] = useState('')
  const change = (event) => setForm((prev) => ({ ...prev, [event.target.name]: event.target.value }))
  const submit = async (event) => {
    event.preventDefault(); setBusy(true); setError('')
    try {
      await request('/api/products', token, { method: 'POST', body: JSON.stringify({ name: form.name, sku: form.sku, category: form.category || null, unit: form.unit, sellingPricePaise: Math.round(Number(form.sellingPrice) * 100), costPricePaise: Math.round(Number(form.costPrice) * 100), initialQuantity: Number(form.initialQuantity), reorderLevel: Number(form.reorderLevel), packSize: Number(form.packSize), leadTimeDays: Number(form.leadTimeDays) }) })
      onCreated()
    } catch (exception) { setError(exception.message) } finally { setBusy(false) }
  }
  return <div className="modal-backdrop" onMouseDown={(e) => { if (e.target === e.currentTarget) onClose() }}><section className="modal-card"><div className="modal-heading"><div><div className="eyebrow">BUILD YOUR CATALOGUE</div><h2>Add a product</h2><p>Start with the essentials. You can adjust details anytime.</p></div><button className="icon-button" onClick={onClose}><X size={19} /></button></div><form onSubmit={submit} className="product-form"><label>Product name<input name="name" value={form.name} onChange={change} placeholder="e.g. Organic Rolled Oats" maxLength="120" required /></label><div className="form-two-col"><label>SKU<input name="sku" value={form.sku} onChange={change} placeholder="e.g. OAT-500" maxLength="64" required /></label><label>Category<input name="category" value={form.category} onChange={change} placeholder="e.g. Pantry" maxLength="80" /></label></div><div className="form-two-col"><label>Unit<select name="unit" value={form.unit} onChange={change}><option value="PCS">Pieces</option><option value="BOX">Box</option><option value="KG">Kilogram</option><option value="LITRE">Litre</option></select></label><label>Opening quantity<input type="number" name="initialQuantity" value={form.initialQuantity} onChange={change} min="0" step="0.001" placeholder="0" required /></label></div><div className="form-two-col"><label>Selling price (₹)<input type="number" name="sellingPrice" value={form.sellingPrice} onChange={change} min="0" step="0.01" placeholder="0.00" required /></label><label>Unit cost (₹)<input type="number" name="costPrice" value={form.costPrice} onChange={change} min="0" step="0.01" placeholder="0.00" required /></label></div><div className="form-three-col"><label>Reorder at<input type="number" name="reorderLevel" value={form.reorderLevel} onChange={change} min="0" step="0.001" placeholder="0" required /></label><label>Pack size<input type="number" name="packSize" value={form.packSize} onChange={change} min="0.001" step="0.001" required /></label><label>Lead time (days)<input type="number" name="leadTimeDays" value={form.leadTimeDays} onChange={change} min="0" max="365" required /></label></div>{error && <div className="form-error"><CircleHelp size={16} />{error}</div>}<div className="modal-actions"><button type="button" className="button button-outline" onClick={onClose}>Cancel</button><button className="button button-dark" disabled={busy}>{busy ? <LoaderCircle className="spin" size={16} /> : <Plus size={16} />} Add product</button></div></form></section></div>
}

function Loading() { return <div className="loading-state"><LoaderCircle className="spin" size={21} />Loading your business data…</div> }
function EmptyState({ icon: Icon, title, text, action, onAction }) { return <div className="empty-state"><span className="empty-icon"><Icon size={21} /></span><strong>{title}</strong><p>{text}</p>{action && <button className="button button-outline" onClick={onAction}>{action} <ArrowRight size={14} /></button>}</div> }

export default App
