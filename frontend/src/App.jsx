import React, { useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import {
  BarChart3,
  Boxes,
  Check,
  ChevronRight,
  ClipboardList,
  LayoutDashboard,
  LogOut,
  Minus,
  Package,
  Plus,
  Search,
  Settings,
  ShoppingBag,
  Store,
  Trash2,
  Truck,
  User
} from 'lucide-react';
import { api, clearAuth, getStoredAuth, storeAuth } from './api';
import './styles.css';

const CART_ID = 'portfolio-demo-cart';
const ORDER_STATUSES = ['CONFIRMED', 'PACKED', 'SHIPPED', 'DELIVERED', 'CANCELLED'];
const emptyCart = { items: [], itemCount: 0, subtotal: 0 };
const currency = new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' });

function App() {
  const [route, setRoute] = useState(window.location.pathname);
  const [auth, setAuth] = useState(getStoredAuth());
  const [products, setProducts] = useState([]);
  const [featured, setFeatured] = useState([]);
  const [slides, setSlides] = useState([]);
  const [adminSlides, setAdminSlides] = useState([]);
  const [categories, setCategories] = useState([]);
  const [cart, setCart] = useState(emptyCart);
  const [orders, setOrders] = useState([]);
  const [adminOrders, setAdminOrders] = useState([]);
  const [dashboard, setDashboard] = useState(null);
  const [query, setQuery] = useState('');
  const [activeCategory, setActiveCategory] = useState('');
  const [cartOpen, setCartOpen] = useState(false);
  const [placedOrder, setPlacedOrder] = useState(null);
  const [authMode, setAuthMode] = useState('login');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const isAdmin = auth.user?.role === 'ADMIN';

  useEffect(() => {
    const onPopState = () => setRoute(window.location.pathname);
    window.addEventListener('popstate', onPopState);
    return () => window.removeEventListener('popstate', onPopState);
  }, []);

  useEffect(() => {
    Promise.all([api.categories(), api.featured(), api.slides()])
      .then(([categoryData, featuredData, slideData]) => {
        setCategories(categoryData);
        setFeatured(featuredData);
        setSlides(slideData);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    loadProducts();
  }, [activeCategory, query]);

  useEffect(() => {
    if (!auth.token) {
      setCart(emptyCart);
      setOrders([]);
      setAdminOrders([]);
      setDashboard(null);
      return;
    }

    refreshCart();
    loadMyOrders();
    if (isAdmin) {
      loadAdminData();
    }
  }, [auth.token, auth.user?.role]);

  function go(path) {
    window.history.pushState({}, '', path);
    setRoute(path);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  async function loadProducts() {
    try {
      setProducts(await api.products({ category: activeCategory, query }));
    } catch (err) {
      setError(err.message);
    }
  }

  async function refreshCart() {
    try {
      setCart(await api.cart(CART_ID));
    } catch (err) {
      setError(err.message);
    }
  }

  async function loadMyOrders() {
    try {
      setOrders(await api.myOrders());
    } catch (err) {
      setError(err.message);
    }
  }

  async function loadAdminData() {
    try {
      const [summary, allOrders, slideData] = await Promise.all([api.dashboard(), api.orders(), api.adminSlides()]);
      setDashboard(summary);
      setAdminOrders(allOrders);
      setAdminSlides(slideData);
    } catch (err) {
      setError(err.message);
    }
  }

  async function submitAuth(event) {
    event.preventDefault();
    setError('');
    const payload = Object.fromEntries(new FormData(event.currentTarget).entries());
    const response = authMode === 'login' ? await api.login(payload) : await api.register(payload);
    storeAuth(response);
    setAuth(getStoredAuth());
  }

  function logout() {
    clearAuth();
    setAuth({ token: null, user: null });
    setPlacedOrder(null);
    go('/');
  }

  async function addToCart(productId) {
    setError('');
    if (!auth.token) {
      setError('Please login before adding items to cart.');
      return;
    }
    setCart(await api.addToCart(CART_ID, productId, 1));
    setCartOpen(true);
  }

  async function updateQuantity(productId, quantity) {
    setError('');
    setCart(await api.updateCartItem(CART_ID, productId, quantity));
  }

  async function checkout(event) {
    event.preventDefault();
    setError('');
    const form = new FormData(event.currentTarget);
    const payload = {
      cartId: CART_ID,
      customer: Object.fromEntries(form.entries()),
      paymentMethod: form.get('paymentMethod')
    };
    const order = await api.checkout(payload);
    setPlacedOrder(order);
    setCart(await api.cart(CART_ID));
    await loadMyOrders();
    if (isAdmin) {
      await loadAdminData();
    }
    go('/orders');
  }

  async function saveProduct(product) {
    setError('');
    const payload = normalizeProductPayload(product);
    if (product.id) {
      await api.updateProduct(product.id, payload);
    } else {
      await api.createProduct(payload);
    }
    await Promise.all([loadProducts(), api.featured().then(setFeatured), loadAdminData()]);
  }

  async function deleteProduct(productId) {
    setError('');
    await api.deleteProduct(productId);
    await Promise.all([loadProducts(), api.featured().then(setFeatured), loadAdminData()]);
  }

  async function uploadImages(files) {
    const selectedFiles = Array.from(files).slice(0, 5);
    const responses = await Promise.all(selectedFiles.map((file) => api.uploadImage(file)));
    return responses.map((response) => response.url);
  }

  async function saveSlide(slide, files) {
    setError('');
    const uploaded = files?.length ? await uploadImages(files) : [];
    const payload = {
      title: slide.title,
      subtitle: slide.subtitle,
      imageUrl: uploaded[0] ?? slide.imageUrl,
      position: Number(slide.position),
      active: Boolean(slide.active)
    };
    if (slide.id) {
      await api.updateSlide(slide.id, payload);
    } else {
      await api.createSlide(payload);
    }
    const [publicSlides, managedSlides] = await Promise.all([api.slides(), api.adminSlides()]);
    setSlides(publicSlides);
    setAdminSlides(managedSlides);
  }

  async function deleteSlide(slideId) {
    setError('');
    await api.deleteSlide(slideId);
    const [publicSlides, managedSlides] = await Promise.all([api.slides(), api.adminSlides()]);
    setSlides(publicSlides);
    setAdminSlides(managedSlides);
  }

  async function updateOrderStatus(orderId, status) {
    setError('');
    await api.updateOrderStatus(orderId, status);
    await Promise.all([loadAdminData(), loadMyOrders()]);
  }

  const page = route.startsWith('/admin') ? (
    <AdminPage
      categories={categories}
      dashboard={dashboard}
      isAdmin={isAdmin}
      orders={adminOrders}
      products={products}
      slides={adminSlides}
      onDeleteProduct={deleteProduct}
      onDeleteSlide={deleteSlide}
      onSaveProduct={saveProduct}
      onSaveSlide={saveSlide}
      onUpdateStatus={updateOrderStatus}
      onUploadImages={uploadImages}
    />
  ) : route.startsWith('/shop') ? (
    <ShopPage
      activeCategory={activeCategory}
      categories={categories}
      loading={loading}
      products={products}
      query={query}
      onAddToCart={addToCart}
      onCategoryChange={setActiveCategory}
      onQueryChange={setQuery}
    />
  ) : route.startsWith('/orders') ? (
    <OrdersPage auth={auth} orders={orders} placedOrder={placedOrder} />
  ) : route.startsWith('/account') ? (
    <AccountPage auth={auth} authMode={authMode} onAuthModeChange={setAuthMode} onLogout={logout} onSubmitAuth={submitAuth} />
  ) : (
    <HomePage featured={featured} slides={slides} onAddToCart={addToCart} onGo={go} />
  );

  return (
    <main>
      <Header auth={auth} cart={cart} isAdmin={isAdmin} route={route} onCartOpen={() => setCartOpen(true)} onGo={go} onLogout={logout} />
      {error ? <p className="global-error">{error}</p> : null}
      {page}
      <CartDrawer
        auth={auth}
        cart={cart}
        open={cartOpen}
        onCheckout={checkout}
        onClose={() => setCartOpen(false)}
        onGo={go}
        onUpdateQuantity={updateQuantity}
      />
    </main>
  );
}

function Header({ auth, cart, isAdmin, route, onCartOpen, onGo, onLogout }) {
  const links = [
    ['/', 'Home', Store],
    ['/shop', 'Shop', ShoppingBag],
    ['/orders', 'Orders', ClipboardList],
    ['/account', 'Account', User],
    ...(isAdmin ? [['/admin', 'Admin', LayoutDashboard]] : [])
  ];

  return (
    <header className="topbar">
      <button className="brand" onClick={() => onGo('/')}>
        <ShoppingBag size={22} />
        <span>CommerceCraft</span>
      </button>
      <nav className="primary-nav">
        {links.map(([path, label, Icon]) => (
          <button key={path} className={route === path ? 'active' : ''} onClick={() => onGo(path)}>
            <Icon size={17} />
            <span>{label}</span>
          </button>
        ))}
      </nav>
      <div className="nav-actions">
        {auth.user ? (
          <button className="icon-pill" onClick={onLogout} title="Logout">
            <LogOut size={17} />
            <span>{auth.user.name}</span>
          </button>
        ) : null}
        <button className="cart-pill" onClick={onCartOpen}>
          <ShoppingBag size={18} />
          <span>{cart.itemCount} items</span>
        </button>
      </div>
    </header>
  );
}

function HomePage({ featured, slides, onAddToCart, onGo }) {
  const heroProduct = featured[0];
  const [activeSlide, setActiveSlide] = useState(0);
  const slide = slides[activeSlide] ?? {
    title: heroProduct?.name ?? 'CommerceCraft',
    subtitle: heroProduct?.description ?? 'A Spring Boot and React ecommerce app with customer shopping flows and a separate admin workspace.',
    imageUrl: heroProduct?.imageUrl
  };

  return (
    <>
      <section className="hero">
        <div className="hero-copy">
          <p className="eyebrow">Full stack storefront</p>
          <h1>{slide.title}</h1>
          <p>{slide.subtitle}</p>
          <div className="hero-actions">
            <button onClick={() => onGo('/shop')}>
              Shop catalog <ChevronRight size={18} />
            </button>
            {heroProduct ? (
              <button className="secondary" onClick={() => onAddToCart(heroProduct.id)}>
                Add featured
              </button>
            ) : null}
          </div>
          {slides.length > 1 ? (
            <div className="slider-dots">
              {slides.map((item, index) => (
                <button
                  key={item.id}
                  className={index === activeSlide ? 'active' : ''}
                  onClick={() => setActiveSlide(index)}
                  title={`Show ${item.title}`}
                />
              ))}
            </div>
          ) : null}
        </div>
        {slide.imageUrl ? (
          <div className="hero-media">
            <img src={slide.imageUrl} alt={slide.title} />
            {heroProduct ? <div className="hero-price">{currency.format(heroProduct.price)}</div> : null}
          </div>
        ) : null}
      </section>

      <section className="featured-strip">
        <SectionTitle eyebrow="Featured" title="Popular products" />
        <div className="product-grid compact">
          {featured.slice(0, 4).map((product) => (
            <ProductCard key={product.id} product={product} onAddToCart={onAddToCart} />
          ))}
        </div>
      </section>
    </>
  );
}

function ShopPage({ activeCategory, categories, loading, products, query, onAddToCart, onCategoryChange, onQueryChange }) {
  return (
    <section className="page-shell">
      <div className="catalog-tools">
        <SectionTitle eyebrow="Shop" title="Products ready for checkout" />
        <label className="search">
          <Search size={18} />
          <input value={query} onChange={(event) => onQueryChange(event.target.value)} placeholder="Search products" />
        </label>
      </div>

      <div className="filters">
        <button className={!activeCategory ? 'active' : ''} onClick={() => onCategoryChange('')}>All</button>
        {categories.map((category) => (
          <button key={category} className={activeCategory === category ? 'active' : ''} onClick={() => onCategoryChange(category)}>
            {formatEnum(category)}
          </button>
        ))}
      </div>

      {loading ? <p className="muted">Loading catalog...</p> : null}
      <div className="product-grid">
        {products.map((product) => (
          <ProductCard key={product.id} product={product} onAddToCart={onAddToCart} />
        ))}
      </div>
    </section>
  );
}

function OrdersPage({ auth, orders, placedOrder }) {
  return (
    <section className="page-shell">
      <SectionTitle eyebrow="Orders" title="Your order history" />
      {!auth.token ? <AuthRequired message="Login to view orders placed with your email address." /> : null}
      {placedOrder ? <p className="success">Order placed: {placedOrder.orderNumber}</p> : null}
      <div className="stack">
        {orders.map((order) => (
          <OrderCard key={order.id} order={order} />
        ))}
        {auth.token && orders.length === 0 ? <EmptyState title="No orders yet" text="Completed checkouts will appear here." /> : null}
      </div>
    </section>
  );
}

function AccountPage({ auth, authMode, onAuthModeChange, onLogout, onSubmitAuth }) {
  if (auth.user) {
    return (
      <section className="page-shell two-column">
        <div>
          <SectionTitle eyebrow="Account" title={auth.user.name} />
          <div className="profile-list">
            <span>Email</span>
            <strong>{auth.user.email}</strong>
            <span>Role</span>
            <strong>{auth.user.role}</strong>
          </div>
        </div>
        <div className="panel">
          <Settings size={26} />
          <h2>Session</h2>
          <p className="muted">Your JWT is stored in local browser storage for this demo frontend.</p>
          <button className="danger" onClick={onLogout}>Logout</button>
        </div>
      </section>
    );
  }

  return (
    <section className="auth-page">
      <div>
        <p className="eyebrow">Secure access</p>
        <h1>Login or create an account</h1>
        <p className="muted">Try customer@commercecraft.test / customer123 or admin@commercecraft.test / admin123.</p>
      </div>
      <form className="auth-form" onSubmit={onSubmitAuth}>
        {authMode === 'register' ? <input name="name" placeholder="Name" required /> : null}
        <input name="email" placeholder="Email" type="email" required />
        <input name="password" placeholder="Password" type="password" required minLength={6} />
        <button>{authMode === 'login' ? 'Login' : 'Create account'}</button>
        <button type="button" className="link-button" onClick={() => onAuthModeChange(authMode === 'login' ? 'register' : 'login')}>
          {authMode === 'login' ? 'Need an account?' : 'Already have an account?'}
        </button>
      </form>
    </section>
  );
}

function AdminPage({
  categories,
  dashboard,
  isAdmin,
  orders,
  products,
  slides,
  onDeleteProduct,
  onDeleteSlide,
  onSaveProduct,
  onSaveSlide,
  onUpdateStatus,
  onUploadImages
}) {
  const [editing, setEditing] = useState(null);
  const [editingSlide, setEditingSlide] = useState(null);

  if (!isAdmin) {
    return (
      <section className="page-shell">
        <AuthRequired message="Admin access is required for product and order management." />
      </section>
    );
  }

  return (
    <section className="admin-layout">
      <aside className="admin-rail">
        <SectionTitle eyebrow="Admin" title="Management" />
        <Metric icon={<BarChart3 />} label="Revenue" value={currency.format(dashboard?.revenue ?? 0)} />
        <Metric icon={<Truck />} label="Orders" value={dashboard?.orders ?? 0} />
        <Metric icon={<Boxes />} label="Products" value={dashboard?.products ?? products.length} />
        <Metric icon={<Package />} label="Low stock" value={dashboard?.lowStockProducts?.length ?? 0} />
      </aside>

      <div className="admin-main">
        <section className="admin-section">
          <div className="section-heading">
            <SectionTitle eyebrow="Homepage" title="Slider images" />
            <button disabled={slides.length >= 5} onClick={() => setEditingSlide(defaultSlide(slides.length + 1))}>
              <Plus size={17} />
              New slide
            </button>
          </div>
          {editingSlide ? (
            <SlideForm
              slide={editingSlide}
              onCancel={() => setEditingSlide(null)}
              onSave={async (slide, files) => {
                await onSaveSlide(slide, files);
                setEditingSlide(null);
              }}
            />
          ) : null}
          <div className="table-list">
            {slides.map((slide) => (
              <div className="table-row slide-row" key={slide.id}>
                <img src={slide.imageUrl} alt={slide.title} />
                <div>
                  <strong>{slide.title}</strong>
                  <span>Position {slide.position} - {slide.active ? 'Active' : 'Hidden'}</span>
                </div>
                <button onClick={() => setEditingSlide(slide)}>Edit</button>
                <button className="danger ghost" onClick={() => onDeleteSlide(slide.id)}>Delete</button>
              </div>
            ))}
          </div>
        </section>

        <section className="admin-section">
          <div className="section-heading">
            <SectionTitle eyebrow="Products" title="Catalog manager" />
            <button onClick={() => setEditing(defaultProduct(categories[0]))}>
              <Plus size={17} />
              New product
            </button>
          </div>
          {editing ? (
            <ProductForm
              categories={categories}
              product={editing}
              onCancel={() => setEditing(null)}
              onSave={async (product) => {
                await onSaveProduct(product);
                setEditing(null);
              }}
              onUploadImages={onUploadImages}
            />
          ) : null}
          <div className="table-list">
            {products.map((product) => (
              <div className="table-row product-row" key={product.id}>
                <img src={product.imageUrl} alt={product.name} />
                <div>
                  <strong>{product.name}</strong>
                  <span>{product.sku} - {formatEnum(product.category)} - {product.stock} stock - {(product.imageUrls?.length ?? 1)} images</span>
                </div>
                <b>{currency.format(product.price)}</b>
                <button onClick={() => setEditing(product)}>Edit</button>
                <button className="danger ghost" onClick={() => onDeleteProduct(product.id)}>Delete</button>
              </div>
            ))}
          </div>
        </section>

        <section className="admin-section">
          <SectionTitle eyebrow="Orders" title="Fulfillment queue" />
          <div className="table-list">
            {orders.map((order) => (
              <div className="table-row order-row" key={order.id}>
                <div>
                  <strong>{order.orderNumber}</strong>
                  <span>{order.customerName} - {order.customerEmail}</span>
                </div>
                <b>{currency.format(order.total)}</b>
                <select value={order.status} onChange={(event) => onUpdateStatus(order.id, event.target.value)}>
                  {ORDER_STATUSES.map((status) => (
                    <option key={status} value={status}>{formatEnum(status)}</option>
                  ))}
                </select>
              </div>
            ))}
          </div>
        </section>
      </div>
    </section>
  );
}

function ProductForm({ categories, product, onCancel, onSave, onUploadImages }) {
  async function submit(event) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const data = Object.fromEntries(form.entries());
    const selectedImages = form.getAll('images').filter((file) => file?.size > 0);
    const manualUrl = data.imageUrl?.trim();
    const existingUrls = product.imageUrls?.length ? product.imageUrls : [product.imageUrl].filter(Boolean);
    const imageUrls = selectedImages.length
      ? await onUploadImages(selectedImages)
      : manualUrl
        ? [manualUrl, ...existingUrls.filter((url) => url !== manualUrl)].slice(0, 5)
        : existingUrls;
    onSave({
      ...product,
      ...data,
      imageUrl: imageUrls[0] ?? data.imageUrl,
      imageUrls,
      price: Number(data.price),
      stock: Number(data.stock),
      rating: Number(data.rating),
      featured: data.featured === 'on'
    });
  }

  return (
    <form className="product-form" onSubmit={submit}>
      <input name="name" defaultValue={product.name} placeholder="Product name" required />
      <input name="sku" defaultValue={product.sku} placeholder="SKU" required />
      <input name="slug" defaultValue={product.slug} placeholder="Slug" />
      <input name="brand" defaultValue={product.brand} placeholder="Brand" required />
      <select name="category" defaultValue={product.category ?? categories[0]} required>
        {categories.map((category) => (
          <option key={category} value={category}>{formatEnum(category)}</option>
        ))}
      </select>
      <input name="imageUrl" defaultValue={product.imageUrl} placeholder="Image URL" />
      <label className="file-field">
        <span>Upload product images</span>
        <input name="images" type="file" accept="image/*" multiple />
      </label>
      <input name="price" defaultValue={product.price} placeholder="Price" type="number" step="0.01" min="0.01" required />
      <input name="stock" defaultValue={product.stock} placeholder="Stock" type="number" min="0" required />
      <input name="rating" defaultValue={product.rating} placeholder="Rating" type="number" step="0.1" min="0" max="5" required />
      <label className="check-row">
        <input name="featured" type="checkbox" defaultChecked={product.featured} />
        Featured
      </label>
      <textarea name="description" defaultValue={product.description} placeholder="Description" required />
      <div className="form-actions">
        <button type="button" className="ghost" onClick={onCancel}>Cancel</button>
        <button>Save product</button>
      </div>
    </form>
  );
}

function SlideForm({ slide, onCancel, onSave }) {
  async function submit(event) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const data = Object.fromEntries(form.entries());
    const files = form.getAll('image').filter((file) => file?.size > 0);
    await onSave({
      ...slide,
      ...data,
      position: Number(data.position),
      active: data.active === 'on'
    }, files);
  }

  return (
    <form className="product-form slide-form" onSubmit={submit}>
      <input name="title" defaultValue={slide.title} placeholder="Slide title" required />
      <input name="subtitle" defaultValue={slide.subtitle} placeholder="Slide subtitle" required />
      <input name="imageUrl" defaultValue={slide.imageUrl} placeholder="Image URL" />
      <input name="position" defaultValue={slide.position} placeholder="Position" type="number" min="1" max="5" required />
      <label className="file-field">
        <span>Upload slide image</span>
        <input name="image" type="file" accept="image/*" />
      </label>
      <label className="check-row">
        <input name="active" type="checkbox" defaultChecked={slide.active} />
        Active
      </label>
      <div className="form-actions">
        <button type="button" className="ghost" onClick={onCancel}>Cancel</button>
        <button>Save slide</button>
      </div>
    </form>
  );
}

function CartDrawer({ auth, cart, open, onCheckout, onClose, onGo, onUpdateQuantity }) {
  const subtotal = useMemo(() => Number(cart.subtotal ?? 0), [cart.subtotal]);

  return (
    <aside className={`drawer ${open ? 'open' : ''}`}>
      <div className="drawer-header">
        <h2>Cart</h2>
        <button onClick={onClose}>Close</button>
      </div>
      {!auth.token ? (
        <div className="notice">
          <p>Login before checkout.</p>
          <button onClick={() => { onClose(); onGo('/account'); }}>Go to account</button>
        </div>
      ) : null}
      {cart.items.length === 0 ? <p className="muted">Your cart is empty.</p> : null}
      {cart.items.map((item) => (
        <div className="cart-row" key={item.productId}>
          <img src={item.imageUrl} alt={item.name} />
          <div>
            <strong>{item.name}</strong>
            <span>{currency.format(item.lineTotal)}</span>
          </div>
          <div className="qty">
            <button onClick={() => onUpdateQuantity(item.productId, item.quantity - 1)}><Minus size={15} /></button>
            <b>{item.quantity}</b>
            <button onClick={() => onUpdateQuantity(item.productId, item.quantity + 1)}><Plus size={15} /></button>
            <button onClick={() => onUpdateQuantity(item.productId, 0)}><Trash2 size={15} /></button>
          </div>
        </div>
      ))}
      <div className="total">
        <span>Subtotal</span>
        <strong>{currency.format(subtotal)}</strong>
      </div>

      <form className="checkout" onSubmit={onCheckout}>
        <input name="fullName" defaultValue={auth.user?.name ?? ''} placeholder="Full name" required />
        <input name="email" defaultValue={auth.user?.email ?? ''} placeholder="Email" type="email" required />
        <input name="phone" placeholder="Phone" required />
        <input name="addressLine" placeholder="Address" required />
        <div className="split">
          <input name="city" placeholder="City" required />
          <input name="state" placeholder="State" required />
        </div>
        <input name="postalCode" placeholder="Postal code" required />
        <select name="paymentMethod" defaultValue="UPI">
          <option value="UPI">UPI</option>
          <option value="CARD">Card</option>
          <option value="CASH_ON_DELIVERY">Cash on delivery</option>
        </select>
        <button disabled={!auth.token || !cart.items.length}>
          Place order <Check size={18} />
        </button>
      </form>
    </aside>
  );
}

function ProductCard({ product, onAddToCart }) {
  return (
    <article className="product-card">
      <img src={product.imageUrl} alt={product.name} />
      <div className="product-body">
        <p>{product.brand}</p>
        <h3>{product.name}</h3>
        <span>{product.description}</span>
        <div className="product-footer">
          <strong>{currency.format(product.price)}</strong>
          <button onClick={() => onAddToCart(product.id)} title={`Add ${product.name}`}>
            <Plus size={18} />
          </button>
        </div>
      </div>
    </article>
  );
}

function OrderCard({ order }) {
  return (
    <article className="order-card">
      <div className="order-head">
        <div>
          <p className="eyebrow">{formatEnum(order.status)}</p>
          <h2>{order.orderNumber}</h2>
        </div>
        <strong>{currency.format(order.total)}</strong>
      </div>
      <p className="muted">{new Date(order.placedAt).toLocaleString()} · {formatEnum(order.paymentMethod)}</p>
      <div className="line-items">
        {order.items.map((item) => (
          <span key={`${order.id}-${item.productId}`}>{item.productName} x {item.quantity}</span>
        ))}
      </div>
    </article>
  );
}

function Metric({ icon, label, value }) {
  return (
    <article className="metric">
      {icon}
      <span>{label}</span>
      <strong>{value}</strong>
    </article>
  );
}

function SectionTitle({ eyebrow, title }) {
  return (
    <div>
      <p className="eyebrow">{eyebrow}</p>
      <h2>{title}</h2>
    </div>
  );
}

function EmptyState({ title, text }) {
  return (
    <div className="empty-state">
      <h2>{title}</h2>
      <p>{text}</p>
    </div>
  );
}

function AuthRequired({ message }) {
  return (
    <div className="empty-state">
      <h2>Access required</h2>
      <p>{message}</p>
    </div>
  );
}

function defaultProduct(category) {
  return {
    sku: '',
    name: '',
    slug: '',
    brand: '',
    description: '',
    category,
    imageUrl: '',
    imageUrls: [],
    price: 1,
    stock: 0,
    rating: 4,
    featured: false
  };
}

function defaultSlide(position) {
  return {
    title: '',
    subtitle: '',
    imageUrl: '',
    position: Math.min(position, 5),
    active: true
  };
}

function normalizeProductPayload(product) {
  const imageUrls = product.imageUrls?.length ? product.imageUrls.slice(0, 5) : [product.imageUrl];
  return {
    sku: product.sku,
    name: product.name,
    slug: product.slug,
    brand: product.brand,
    description: product.description,
    category: product.category,
    imageUrl: imageUrls[0],
    imageUrls,
    price: Number(product.price),
    stock: Number(product.stock),
    rating: Number(product.rating),
    featured: Boolean(product.featured)
  };
}

function formatEnum(value) {
  return String(value ?? '').replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase());
}

createRoot(document.getElementById('root')).render(<App />);
