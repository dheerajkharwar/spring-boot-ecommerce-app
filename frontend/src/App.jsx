import React, { useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import {
  BarChart3,
  Boxes,
  Check,
  ChevronRight,
  Minus,
  Package,
  Plus,
  Search,
  ShoppingBag,
  Sparkles,
  Trash2,
  Truck
} from 'lucide-react';
import { api, clearAuth, getStoredAuth, storeAuth } from './api';
import './styles.css';

const CART_ID = 'portfolio-demo-cart';
const currency = new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' });

function App() {
  const [products, setProducts] = useState([]);
  const [featured, setFeatured] = useState([]);
  const [categories, setCategories] = useState([]);
  const [cart, setCart] = useState({ items: [], itemCount: 0, subtotal: 0 });
  const [dashboard, setDashboard] = useState(null);
  const [activeCategory, setActiveCategory] = useState('');
  const [query, setQuery] = useState('');
  const [checkoutOpen, setCheckoutOpen] = useState(false);
  const [order, setOrder] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [auth, setAuth] = useState(getStoredAuth());
  const [authMode, setAuthMode] = useState('login');

  useEffect(() => {
    Promise.all([api.categories(), api.featured()])
      .then(([categoryData, featuredData]) => {
        setCategories(categoryData);
        setFeatured(featuredData);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (!auth.token) {
      setCart({ items: [], itemCount: 0, subtotal: 0 });
      setDashboard(null);
      return;
    }

    api.cart(CART_ID)
      .then(setCart)
      .catch((err) => setError(err.message));

    if (auth.user?.role === 'ADMIN') {
      api.dashboard()
        .then(setDashboard)
        .catch((err) => setError(err.message));
    }
  }, [auth]);

  useEffect(() => {
    api.products({ category: activeCategory, query })
      .then(setProducts)
      .catch((err) => setError(err.message));
  }, [activeCategory, query]);

  const heroProduct = featured[0];
  const total = useMemo(() => Number(cart.subtotal ?? 0), [cart.subtotal]);

  async function addToCart(productId) {
    setError('');
    if (!auth.token) {
      setError('Please login before adding items to cart.');
      return;
    }
    setCart(await api.addToCart(CART_ID, productId, 1));
  }

  async function updateQuantity(productId, quantity) {
    setError('');
    if (!auth.token) {
      setError('Please login to update cart items.');
      return;
    }
    setCart(await api.updateCartItem(CART_ID, productId, quantity));
  }

  async function checkout(event) {
    event.preventDefault();
    setError('');
    if (!auth.token) {
      setError('Please login before checkout.');
      return;
    }
    const form = new FormData(event.currentTarget);
    const payload = {
      cartId: CART_ID,
      customer: Object.fromEntries(form.entries()),
      paymentMethod: form.get('paymentMethod')
    };
    const placedOrder = await api.checkout(payload);
    setOrder(placedOrder);
    setCart(await api.cart(CART_ID));
    if (auth.user?.role === 'ADMIN') {
      setDashboard(await api.dashboard());
    }
  }

  async function submitAuth(event) {
    event.preventDefault();
    setError('');
    const form = new FormData(event.currentTarget);
    const payload = Object.fromEntries(form.entries());
    const response = authMode === 'login'
      ? await api.login(payload)
      : await api.register(payload);
    storeAuth(response);
    setAuth(getStoredAuth());
  }

  function logout() {
    clearAuth();
    setAuth({ token: null, user: null });
    setOrder(null);
  }

  return (
    <main>
      <nav className="topbar">
        <div className="brand">
          <ShoppingBag size={22} />
          <span>CommerceCraft</span>
        </div>
        <div className="nav-actions">
          {auth.user ? (
            <button className="user-pill" onClick={logout}>
              {auth.user.name} · Logout
            </button>
          ) : null}
          <button className="cart-pill" onClick={() => setCheckoutOpen(true)}>
            <ShoppingBag size={18} />
            <span>{cart.itemCount} items</span>
          </button>
        </div>
      </nav>

      {!auth.user ? (
        <section className="auth-panel">
          <div>
            <p className="eyebrow">Secure demo</p>
            <h2>Login to use cart and checkout</h2>
            <p className="muted">Try customer: customer@commercecraft.test / customer123</p>
            <p className="muted">Try admin: admin@commercecraft.test / admin123</p>
          </div>
          <form onSubmit={submitAuth}>
            {authMode === 'register' ? <input name="name" placeholder="Name" required /> : null}
            <input name="email" placeholder="Email" type="email" required />
            <input name="password" placeholder="Password" type="password" required minLength={6} />
            <button>{authMode === 'login' ? 'Login' : 'Create account'}</button>
            <button type="button" className="link-button" onClick={() => setAuthMode(authMode === 'login' ? 'register' : 'login')}>
              {authMode === 'login' ? 'Need an account?' : 'Already have an account?'}
            </button>
          </form>
        </section>
      ) : null}

      <section className="hero">
        <div className="hero-copy">
          <p className="eyebrow"><Sparkles size={16} /> Curated drop</p>
          <h1>{heroProduct?.name ?? 'CommerceCraft Storefront'}</h1>
          <p>{heroProduct?.description ?? 'A full-stack ecommerce showcase backed by Spring Boot APIs.'}</p>
          <button onClick={() => heroProduct && addToCart(heroProduct.id)} disabled={!heroProduct}>
            Add featured item <ChevronRight size={18} />
          </button>
        </div>
        {heroProduct && (
          <div className="hero-media">
            <img src={heroProduct.imageUrl} alt={heroProduct.name} />
            <div className="hero-price">{currency.format(heroProduct.price)}</div>
          </div>
        )}
      </section>

      <section className="metrics">
        <Metric icon={<BarChart3 />} label="Revenue" value={currency.format(dashboard?.revenue ?? 0)} />
        <Metric icon={<Truck />} label="Orders" value={dashboard?.orders ?? 0} />
        <Metric icon={<Boxes />} label="Products" value={dashboard?.products ?? 0} />
        <Metric icon={<Package />} label="Low stock" value={dashboard?.lowStockProducts?.length ?? 0} />
      </section>

      <section className="catalog">
        <div className="catalog-tools">
          <div>
            <p className="eyebrow">Catalog</p>
            <h2>Products ready for checkout</h2>
          </div>
          <label className="search">
            <Search size={18} />
            <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search products" />
          </label>
        </div>

        <div className="filters">
          <button className={!activeCategory ? 'active' : ''} onClick={() => setActiveCategory('')}>All</button>
          {categories.map((category) => (
            <button
              key={category}
              className={activeCategory === category ? 'active' : ''}
              onClick={() => setActiveCategory(category)}
            >
              {category.replaceAll('_', ' ').toLowerCase()}
            </button>
          ))}
        </div>

        {loading ? <p className="muted">Loading catalog...</p> : null}
        {error ? <p className="error">{error}</p> : null}

        <div className="product-grid">
          {products.map((product) => (
            <article className="product-card" key={product.id}>
              <img src={product.imageUrl} alt={product.name} />
              <div className="product-body">
                <p>{product.brand}</p>
                <h3>{product.name}</h3>
                <span>{product.description}</span>
                <div className="product-footer">
                  <strong>{currency.format(product.price)}</strong>
                  <button onClick={() => addToCart(product.id)} title={`Add ${product.name}`}>
                    <Plus size={18} />
                  </button>
                </div>
              </div>
            </article>
          ))}
        </div>
      </section>

      <aside className={`drawer ${checkoutOpen ? 'open' : ''}`}>
        <div className="drawer-header">
          <h2>Cart</h2>
          <button onClick={() => setCheckoutOpen(false)}>Close</button>
        </div>
        {cart.items.length === 0 ? <p className="muted">Your cart is empty.</p> : null}
        {cart.items.map((item) => (
          <div className="cart-row" key={item.productId}>
            <img src={item.imageUrl} alt={item.name} />
            <div>
              <strong>{item.name}</strong>
              <span>{currency.format(item.lineTotal)}</span>
            </div>
            <div className="qty">
              <button onClick={() => updateQuantity(item.productId, item.quantity - 1)}><Minus size={15} /></button>
              <b>{item.quantity}</b>
              <button onClick={() => updateQuantity(item.productId, item.quantity + 1)}><Plus size={15} /></button>
              <button onClick={() => updateQuantity(item.productId, 0)}><Trash2 size={15} /></button>
            </div>
          </div>
        ))}
        <div className="total">
          <span>Subtotal</span>
          <strong>{currency.format(total)}</strong>
        </div>

        <form className="checkout" onSubmit={checkout}>
          <input name="fullName" placeholder="Full name" required />
          <input name="email" placeholder="Email" type="email" required />
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
          <button disabled={!cart.items.length}>
            Place order <Check size={18} />
          </button>
        </form>
        {order ? <p className="success">Order placed: {order.orderNumber}</p> : null}
      </aside>
    </main>
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

createRoot(document.getElementById('root')).render(<App />);
