const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api';
const TOKEN_KEY = 'commercecraft_token';
const USER_KEY = 'commercecraft_user';

export function getStoredAuth() {
  const token = localStorage.getItem(TOKEN_KEY);
  const user = localStorage.getItem(USER_KEY);
  return {
    token,
    user: user ? JSON.parse(user) : null
  };
}

export function storeAuth(auth) {
  localStorage.setItem(TOKEN_KEY, auth.token);
  localStorage.setItem(USER_KEY, JSON.stringify({
    userId: auth.userId,
    name: auth.name,
    email: auth.email,
    role: auth.role
  }));
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

async function request(path, options = {}) {
  const token = localStorage.getItem(TOKEN_KEY);
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers ?? {})
    },
    ...options
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: response.statusText }));
    throw new Error(error.message ?? 'Request failed');
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

export const api = {
  login: (payload) =>
    request('/auth/login', {
      method: 'POST',
      body: JSON.stringify(payload)
    }),
  register: (payload) =>
    request('/auth/register', {
      method: 'POST',
      body: JSON.stringify(payload)
    }),
  categories: () => request('/categories'),
  products: (params = {}) => {
    const search = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value) search.set(key, value);
    });
    const suffix = search.toString() ? `?${search}` : '';
    return request(`/products${suffix}`);
  },
  featured: () => request('/products/featured'),
  cart: (cartId) => request(`/carts/${cartId}`),
  addToCart: (cartId, productId, quantity = 1) =>
    request(`/carts/${cartId}/items`, {
      method: 'POST',
      body: JSON.stringify({ productId, quantity })
    }),
  updateCartItem: (cartId, productId, quantity) =>
    request(`/carts/${cartId}/items/${productId}`, {
      method: 'PUT',
      body: JSON.stringify(quantity)
    }),
  checkout: (payload) =>
    request('/orders', {
      method: 'POST',
      body: JSON.stringify(payload)
    }),
  dashboard: () => request('/dashboard/summary')
};
