// ── State ────────────────────────────────────────────────────────
let CARS = [];
let BOOKINGS = [];
let selectedBookingId = localStorage.getItem('senet_pending_booking') || null;
let selectedBookingAmount = parseFloat(localStorage.getItem('senet_pending_amount')) || 0;

// ── Utils ─────────────────────────────────────────────────────────
function $id(id) { return document.getElementById(id); }
function setText(id, val) { const el = $id(id); if (el) el.textContent = val; }

// ── Car Card HTML ─────────────────────────────────────────────────
function carCard(car) {
  const badge = car.status === 'available'
    ? '<span class="badge badge-available">Available</span>'
    : '<span class="badge badge-booked">Booked</span>';
  const img = car.imageUrl || 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&q=80';
  return `
    <div class="car-card" onclick="openCarDetail(${car.id})">
      <div class="car-card-img-wrap">
        <img class="car-card-img" src="${img}" alt="${car.brand} ${car.model}" loading="lazy"
             onerror="this.src='https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&q=80'">
        <div class="car-card-tag">${badge}</div>
      </div>
      <div class="car-card-body">
        <div class="car-card-brand">${car.brand} · ${car.year}</div>
        <div class="car-card-name">${car.model}</div>
        <div class="car-card-specs">
          <div class="car-spec"><div class="val">${car.transmission || 'Auto'}</div><div class="key">Transmission</div></div>
          <div class="car-spec"><div class="val">${car.category || 'Luxury'}</div><div class="key">Class</div></div>
        </div>
        <div class="car-card-footer">
          <div class="car-price">
            <span class="amount">EGP ${car.pricePerDay.toLocaleString()}</span>
            <span class="per">/day</span>
          </div>
          <button class="btn btn-gold btn-sm" onclick="event.stopPropagation(); bookCar(${car.id})">Book</button>
        </div>
      </div>
    </div>`;
}

// ── Book / View Car ───────────────────────────────────────────────
function bookCar(carId) {
  window.selectedCarId = carId;
  if (!getToken()) { showPage('page-login'); showToast('Please login to book'); return; }
  switchDash('dash-booking-form', $id('nav-new-booking'));
}

function openCarDetail(carId) {
  window.selectedCarId = carId;
  if (!getToken()) { showPage('page-login'); showToast('Please login to view details'); return; }
  switchDash('dash-car-detail', null);
}

function goBackToFleet() {
  const role = getRole();
  if (role === 'ADMIN' || role === 'EMPLOYEE') switchDash('dash-cars', $id('nav-cars'));
  else switchDash('dash-client-cars', $id('nav-client-cars'));
}

// ── Render Cars ───────────────────────────────────────────────────
async function renderCars() {
  const role = getRole();
  try {
    CARS = await apiGetCars() || [];
    const empty = '<p style="color:var(--white-dim);text-align:center;padding:40px;">No vehicles found.</p>';

    if ($id('home-cars-grid'))
      $id('home-cars-grid').innerHTML = CARS.length ? CARS.slice(0, 3).map(carCard).join('') : empty;
    if ($id('public-cars-grid'))
      $id('public-cars-grid').innerHTML = CARS.length ? CARS.map(carCard).join('') : empty;

    if ($id('cars-table-body')) {
      $id('cars-table-body').innerHTML = CARS.length
        ? CARS.map(c => `
            <tr>
              <td><div class="table-car-cell">
                <img class="table-car-thumb" src="${c.imageUrl||''}" alt="" onerror="this.style.display='none'">
                <div class="table-car-info">
                  <div class="car-name">${c.brand} ${c.model}</div>
                  <div class="car-brand">${c.category}</div>
                </div>
              </div></td>
              <td>${c.category}</td>
              <td style="color:var(--gold)">EGP ${c.pricePerDay.toLocaleString()}</td>
              <td><span class="badge ${c.status==='available'?'badge-available':'badge-booked'}">${c.status}</span></td>
              <td>${c.year}</td>
              <td><div class="action-btns">
              ${role === 'ADMIN' ? `<button class="btn-icon" onclick="editCar(${c.id})" title="Edit">✏</button>` : ''}
              <button class="btn-icon" onclick="openCarDetail(${c.id})" title="View">👁</button>
              ${role === 'ADMIN' ? `<button class="btn-icon del" onclick="deleteCar(${c.id})" title="Delete">✕</button>` : ''}
              </div></td>
            </tr>`).join('')
        : '<tr><td colspan="6" style="text-align:center;color:var(--white-dim);padding:32px;">No vehicles found.</td></tr>';
    }

    setText('stat-total-cars', CARS.length);
    setText('fleet-count-text', `${CARS.length} vehicle${CARS.length !== 1 ? 's' : ''} in fleet`);
    filterClientCars();
  } catch (err) {
    console.error('Failed to load cars', err);
  }
}

function editCar(id) {
  const car = CARS.find(c => c.id === id);
  if (!car) return;
  window.editingCarId = id;
  const set = (elId, val) => { const el = $id(elId); if (el) el.value = val || ''; };
  set('add-car-brand',        car.brand);
  set('add-car-model',        car.model);
  set('add-car-year',         car.year);
  set('add-car-price',        car.pricePerDay);
  set('add-car-engine',       car.engine);
  set('add-car-image',        car.imageUrl);
  set('add-car-description',  car.description);
  const cap = s => s ? s.charAt(0).toUpperCase() + s.slice(1) : '';
  if ($id('add-car-status'))       $id('add-car-status').value       = cap(car.status) || 'Available';
  if ($id('add-car-transmission')) $id('add-car-transmission').value = car.transmission || 'Automatic';
  if ($id('add-car-category'))     $id('add-car-category').value     = car.category || '';
  setText('add-car-form-title', 'Edit Vehicle');
  setText('add-car-form-label', 'Fleet Management · Edit');
  switchDash('dash-add-car', null);
}

async function deleteCar(id) {
  if (!confirm('Remove this vehicle from the fleet?')) return;
  try {
    await apiDeleteCar(id);
    showToast('Vehicle removed');
    await renderCars();
    switchDash('dash-cars', $id('nav-cars'));
  } catch (err) {
    showToast('Error: ' + err.message);
  }
}

// ── Client Browse Cars ────────────────────────────────────────────
async function renderClientCars() {
  if (CARS.length === 0) {
    try { CARS = await apiGetCars() || []; } catch (e) { console.error(e); }
  }
  filterClientCars();
}

function filterClientCars() {
  const grid = $id('client-cars-grid');
  if (!grid) return;
  const search = ($id('client-search')?.value || '').toLowerCase();
  const category = $id('client-category-filter')?.value || '';

  const filtered = CARS.filter(c => {
    if (c.status !== 'available') return false;
    if (search && !`${c.brand} ${c.model}`.toLowerCase().includes(search)) return false;
    if (category && c.category !== category) return false;
    return true;
  });

  grid.innerHTML = filtered.length
    ? filtered.map(carCard).join('')
    : '<p style="color:var(--white-dim);text-align:center;padding:40px;">No available vehicles match your search.</p>';
}

// ── Revenue Calculation ───────────────────────────────────────────
function computeRevenue() {
  const total = BOOKINGS
    .filter(b => b.status === 'confirmed' || b.status === 'completed')
    .reduce((sum, b) => sum + (parseFloat((b.amount || '').replace(/[^0-9.]/g, '')) || 0), 0);
  return total > 0 ? 'EGP ' + total.toLocaleString() : '—';
}

// ── Render Bookings ───────────────────────────────────────────────
async function renderBookings() {
  if (!getToken()) return;
  const bookingsTbody = $id('bookings-table-body');
  if (bookingsTbody) bookingsTbody.innerHTML = '<tr><td colspan="8" style="text-align:center;color:var(--white-dim);padding:32px;">Loading...</td></tr>';
  try {
    const role = getRole();
    BOOKINGS = (role === 'ADMIN' || role === 'EMPLOYEE')
      ? (await apiGetAllBookings() || [])
      : (await apiGetMyBookings() || []);

    const statusCls = {
      confirmed: 'badge-available', pending: 'badge-pending',
      completed: 'badge-available', cancelled: 'badge-cancelled'
    };

    filterBookingsTable();

    if ($id('overview-bookings-body')) {
      $id('overview-bookings-body').innerHTML = BOOKINGS.slice(0, 5).map(b => `
        <tr>
          <td>${b.car || b.carId}</td>
          <td>${b.client || b.userId}</td>
          <td>${b.pickup} – ${b.ret}</td>
          <td style="color:var(--gold)">${b.amount}</td>
          <td><span class="badge ${statusCls[b.status]||'badge-pending'}">${b.status}</span></td>
        </tr>`).join('') || '<tr><td colspan="5" style="text-align:center;color:var(--white-dim);">No bookings yet.</td></tr>';
    }

    if ($id('profile-bookings-body')) {
      $id('profile-bookings-body').innerHTML = BOOKINGS.length
        ? BOOKINGS.slice(0, 10).map(b => `
            <tr>
              <td>${b.car || b.carId}</td>
              <td>${b.pickup} – ${b.ret}</td>
              <td style="color:var(--gold)">${b.amount}</td>
              <td><span class="badge ${statusCls[b.status]||'badge-pending'}">${b.status}</span></td>
            </tr>`).join('')
        : '<tr><td colspan="4" style="text-align:center;color:var(--white-dim);">No bookings yet.</td></tr>';
    }

    setText('stat-total-bookings', BOOKINGS.length);
    setText('stat-pending', BOOKINGS.filter(b => b.status === 'pending').length);
    setText('stat-revenue', computeRevenue());
    setText('bookings-count', BOOKINGS.length);
    setText('bookings-page-label', (role === 'ADMIN' || role === 'EMPLOYEE') ? 'All Bookings' : 'My Bookings');
    setText('profile-bookings-count', BOOKINGS.length);
  } catch (err) {
    console.error('Failed to load bookings', err);
  }
}

function filterBookingsTable() {
  const tbody = $id('bookings-table-body');
  if (!tbody) return;
  const statusFilter = ($id('bookings-filter-status')?.value || '').toLowerCase();
  const role = getRole();
  const statusCls = {
    confirmed: 'badge-available', pending: 'badge-pending',
    completed: 'badge-available', cancelled: 'badge-cancelled'
  };
  const rows = statusFilter ? BOOKINGS.filter(b => b.status === statusFilter) : BOOKINGS;
  tbody.innerHTML = rows.length
    ? rows.map(b => `
        <tr>
          <td style="color:var(--silver-dim);font-size:11px;">${b.id}</td>
          <td>${b.car || b.carId}</td>
          <td>${b.client || b.userId}</td>
          <td>${b.pickup}</td>
          <td>${b.ret}</td>
          <td style="color:var(--gold)">${b.amount}</td>
          <td><span class="badge ${statusCls[b.status]||'badge-pending'}">${b.status}</span></td>
          <td><div class="action-btns">
            ${(role === 'ADMIN' || role === 'EMPLOYEE') ? `
              <select onchange="changeBookingStatus('${b.id}',this.value)" style="background:var(--surface);border:1px solid var(--border);color:var(--white);padding:3px 6px;border-radius:4px;font-size:11px;font-family:var(--font-ui);">
                <option value="pending"   ${b.status==='pending'  ?'selected':''}>Pending</option>
                <option value="confirmed" ${b.status==='confirmed'?'selected':''}>Confirmed</option>
                <option value="completed" ${b.status==='completed'?'selected':''}>Completed</option>
                <option value="cancelled" ${b.status==='cancelled'?'selected':''}>Cancelled</option>
              </select>` : ''}
            ${role === 'ADMIN' ? `
             <button class="btn-icon del" onclick="deleteBooking('${b.id}')" title="Delete">🗑</button>` : ''}
          </div></td>
        </tr>`).join('')
    : '<tr><td colspan="8" style="text-align:center;color:var(--white-dim);padding:32px;">No bookings found.</td></tr>';
}

async function deleteBooking(id) {
  if (!confirm('Delete this booking permanently?')) return;
  try {
    await apiDeleteBooking(id);
    showToast('Booking deleted');
    await renderBookings();
  } catch (err) { showToast('Error: ' + err.message); }
}

async function changeBookingStatus(id, status) {
  try {
    await apiUpdateBookingStatus(id, status);
    showToast('Status updated');
    await renderBookings();
  } catch (err) { showToast('Error: ' + err.message); }
}

async function cancelBooking(id) {
  try {
    await apiFetch(`/api/bookings/${id}/cancel`, { method: 'PATCH' });
    showToast('Booking cancelled');
    await renderBookings();
  } catch (err) {
    showToast('Error: ' + err.message);
  }
}

// ── Save New Car ──────────────────────────────────────────────────
async function saveNewCar() {
  const brand = $id('add-car-brand').value.trim();
  const model = $id('add-car-model').value.trim();
  const year = parseInt($id('add-car-year').value) || new Date().getFullYear();
  const category = $id('add-car-category').value;
  const pricePerDay = parseFloat($id('add-car-price').value);
  const status = $id('add-car-status').value.toLowerCase();
  const engine = $id('add-car-engine').value.trim();
  const transmission = $id('add-car-transmission').value;
  const imageUrl = $id('add-car-image').value.trim();
  const description = $id('add-car-description').value.trim();

  if (!brand || !model || !pricePerDay) { showToast('Please fill required fields: brand, model, price'); return; }

  const btn = document.querySelector('#dash-add-car .btn-gold');
  btn.textContent = 'Saving...'; btn.disabled = true;
  try {
    if (window.editingCarId) {
      await apiUpdateCar(window.editingCarId, { brand, model, year, category, pricePerDay, status, engine, transmission, imageUrl, description });
      window.editingCarId = null;
      showToast('Vehicle updated ✦');
    } else {
      await apiCreateCar({ brand, model, year, category, pricePerDay, status, engine, transmission, imageUrl, description });
      showToast('Vehicle added to fleet ✦');
    }
    ['add-car-brand','add-car-model','add-car-year','add-car-price','add-car-engine','add-car-image','add-car-description']
      .forEach(id => { const el = $id(id); if (el) el.value = ''; });
    setText('add-car-form-title', 'Add New Vehicle');
    setText('add-car-form-label', 'Fleet Management');
    await renderCars();
    switchDash('dash-cars', $id('nav-cars'));
  } catch (err) {
    showToast('Error: ' + err.message);
  } finally {
    btn.textContent = 'Save Vehicle'; btn.disabled = false;
  }
}

// ── Profile ───────────────────────────────────────────────────────
async function updateProfile() {
  const name       = $id('profile-name-input').value.trim();
  const email      = $id('profile-email-input').value.trim();
  const phone      = $id('profile-phone-input').value.trim();
  const nationalId = $id('profile-id-input').value.trim();

  if (!name || !email) { showToast('Name and email are required'); return; }  // ← was showToast()

  try {
    const data = await apiUpdateMyProfile({ name, email, phone, nationalId });
    showToast('Profile updated ✦');
    if (data.name)  localStorage.setItem('senet_name', data.name);
    if (data.email) localStorage.setItem('senet_email', data.email);
    applyRoleUI();
  } catch (err) { showToast(err.message); }  // ← was showToast()
}

async function loadProfileData() {
  try {
    const p = await apiGetMyProfile();
    if (!p) return;
    if ($id('profile-name-input'))  $id('profile-name-input').value  = p.name       || '';
    if ($id('profile-email-input')) $id('profile-email-input').value = p.email      || '';
    if ($id('profile-phone-input')) $id('profile-phone-input').value = p.phone      || '';
    if ($id('profile-id-input'))    $id('profile-id-input').value    = p.nationalId || '';
    setText('profile-name-header',  p.name  || '');
    setText('profile-email-header', p.email || '');
  } catch (err) { console.error('Failed to load profile', err); }
}

async function changePassword() {
  const curr    = $id('profile-curr-pass')?.value;
  const next    = $id('profile-new-pass')?.value;
  const confirm = $id('profile-confirm-pass')?.value;

  // Replace all alerts with showToast:
  if (!curr || !next || !confirm) { showToast('Please fill all password fields'); return; }
  if (next !== confirm)           { showToast('New passwords do not match'); return; }
  if (next.length < 6)            { showToast('Password must be at least 6 characters'); return; }

  try {
    await apiChangePassword(curr, next);
    showToast('Password updated successfully ✦');
    ['profile-curr-pass','profile-new-pass','profile-confirm-pass']
      .forEach(id => { const el = $id(id); if (el) el.value = ''; });
  } catch (err) { showToast('Failed: ' + err.message); }
}

// ── Booking Form ──────────────────────────────────────────────────
function populateBookingCarSelect() {
  const sel = $id('booking-car-select');
  if (!sel) return;
  const available = CARS.filter(c => c.status === 'available');
  if (!available.length) { sel.innerHTML = '<option>No vehicles available</option>'; return; }
  sel.innerHTML = available.map(c =>
    `<option value="${c.id}" data-price="${c.pricePerDay}">${c.brand} ${c.model} — EGP ${c.pricePerDay.toLocaleString()}/day</option>`
  ).join('');
  if (window.selectedCarId) sel.value = window.selectedCarId;
  updateBookingCalc();
}

function updateBookingCalc() {
  const sel = $id('booking-car-select');
  const pickup = $id('booking-pickup')?.value;
  const ret = $id('booking-return')?.value;
  if (!sel || !sel.options.length) return;
  const opt = sel.options[sel.selectedIndex];
  const pricePerDay = opt ? parseFloat(opt.dataset.price) || 0 : 0;
  let days = 1;
  if (pickup && ret) {
    const diff = Math.ceil((new Date(ret) - new Date(pickup)) / 86400000);
    if (diff > 0) days = diff;
  }
  const subtotal = pricePerDay * days;
  const total = subtotal + 300;
  selectedBookingAmount = total;
  setText('booking-summary-car', opt ? opt.text.split(' — ')[0] : '—');
  setText('booking-summary-days', days + ' Day' + (days !== 1 ? 's' : ''));
  setText('booking-summary-rate', 'EGP ' + pricePerDay.toLocaleString());
  setText('booking-summary-subtotal', 'EGP ' + subtotal.toLocaleString());
  setText('booking-summary-total', 'EGP ' + total.toLocaleString());
}

async function confirmBooking() {
  const sel = $id('booking-car-select');
  const clientName = $id('booking-client-name')?.value.trim();
  const pickup = $id('booking-pickup')?.value;
  const ret = $id('booking-return')?.value;
  if (!sel || !clientName || !pickup || !ret) { showToast('Please fill all required fields'); return; }
  if (new Date(ret) <= new Date(pickup)) { showToast('Return date must be after pickup date'); return; }

  const carId = parseInt(sel.value);
  const car = CARS.find(c => c.id === carId);
  const carName = car ? `${car.brand} ${car.model}` : 'Vehicle';
  const days = Math.ceil((new Date(ret) - new Date(pickup)) / 86400000);
  const pricePerDay = car ? car.pricePerDay : 0;
  const total = pricePerDay * days + 300;
  selectedBookingAmount = total;

  const btn = $id('confirm-booking-btn');
  if (btn) { btn.textContent = 'Creating...'; btn.disabled = true; }
  try {
    const booking = await apiCreateBooking({
      carId, car: carName, client: clientName, pickup, ret,
      amount: 'EGP ' + total.toLocaleString()
    });
    selectedBookingId = booking.id;
    localStorage.setItem('senet_pending_booking', booking.id);
    localStorage.setItem('senet_pending_amount', total); 
    showToast('Booking created! Complete payment ✦');
    renderCars(); // refresh status in background — don't await, just fire
    setText('pay-booking-id', booking.id);
    setText('pay-summary-car', carName);
    setText('pay-car-name', carName);
    setText('pay-summary-pickup', pickup);
    setText('pay-summary-return', ret);
    setText('pay-summary-days', days + ' Day' + (days !== 1 ? 's' : ''));
    setText('pay-summary-rate', 'EGP ' + pricePerDay.toLocaleString());
    setText('pay-summary-subtotal', 'EGP ' + (pricePerDay * days).toLocaleString());
    setText('pay-summary-total', 'EGP ' + total.toLocaleString());
    const payBtn = $id('pay-btn');
    if (payBtn) payBtn.textContent = '✦ Pay EGP ' + total.toLocaleString();
    switchDash('dash-payment', null);
  } catch (err) {
    showToast(err.message);
  } finally {
    if (btn) { btn.textContent = 'Confirm Booking'; btn.disabled = false; }
  }
}

// ── Payment ───────────────────────────────────────────────────────
async function renderPayments() {
  if (!getToken()) return;
  const tbody = $id('payments-table-body');
  if (tbody) {
    tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;color:var(--white-dim);padding:32px;">Loading...</td></tr>';
  }
  try {
    const role = getRole();
    const payments = (role === 'ADMIN')
      ? (await apiGetAllPayments() || [])
      : (await apiGetMyPayments() || []);
    const tbody = $id('payments-table-body');
    if (!tbody) return;
    tbody.innerHTML = payments.length
      ? payments.map(p => `
          <tr>
            <td style="color:var(--silver-dim);font-size:11px;">${p.id}</td>
            <td style="color:var(--silver-dim);font-size:11px;">${p.bookingId}</td>
            <td>•••• ${p.cardNumber || '????'}</td>
            <td>${p.expiry || '—'}</td>
            <td style="color:var(--gold)">EGP ${(p.amount || 0).toLocaleString()}</td>
            <td><span class="badge badge-available">${p.status || 'success'}</span></td>
          </tr>`).join('')
      : '<tr><td colspan="6" style="text-align:center;color:var(--white-dim);padding:32px;">No payments yet.</td></tr>';
    setText('payments-count', payments.length);
    setText('payments-page-label', role === 'ADMIN' ? 'All Payments' : 'My Payments');
  } catch (err) {
    console.error('Failed to load payments', err);
  }
}

async function renderUsers() {
  const tbody = $id('users-table-body');
  if (!tbody) return;
  tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;color:var(--white-dim);padding:32px;">Loading...</td></tr>';
  try {
    const users = await apiGetAllUsers() || [];
    tbody.innerHTML = users.length
      ? users.map(u => `
          <tr>
            <td>${u.name || '—'}</td>
            <td style="font-size:11px;color:var(--silver-dim);">${u.email}</td>
            <td>${u.phone || '—'}</td>
            <td>
              <select onchange="updateUserRole('${u.id}',this.value)"
                style="background:var(--surface);border:1px solid var(--border);color:var(--white);padding:3px 6px;border-radius:4px;font-size:11px;font-family:var(--font-ui);">
                <option value="CLIENT"   ${u.role==='CLIENT'  ?'selected':''}>Client</option>
                <option value="EMPLOYEE" ${u.role==='EMPLOYEE'?'selected':''}>Employee</option>
                <option value="ADMIN"    ${u.role==='ADMIN'   ?'selected':''}>Admin</option>
              </select>
            </td>
            <td><div class="action-btns">
              <button class="btn-icon del" onclick="deleteUser('${u.id}')" title="Delete">✕</button>
            </div></td>
          </tr>`).join('')
      : '<tr><td colspan="5" style="text-align:center;color:var(--white-dim);padding:32px;">No users found.</td></tr>';
    setText('users-count', users.length);
  } catch (err) { console.error('Failed to load users', err); }
}

async function updateUserRole(id, role) {
  if (id === getUserId()) {
    showToast('You cannot change your own role');
    await renderUsers(); // re-render to reset the dropdown visually
    return;
  }
  try {
    await apiUpdateUser(id, { role });
    showToast('Role updated ✦');
    await renderUsers(); // re-render so table reflects the saved state
  } catch (err) {
    showToast('Error: ' + err.message);
    await renderUsers(); // re-render to reset dropdown on failure too
  }
}

async function deleteUser(id) {
  if (id === getUserId()) {
    showToast('You cannot delete your own account');
    return;
  }
  if (!confirm('Delete this user permanently?')) return;
  try {
    await apiDeleteUser(id);
    showToast('User deleted');
    await renderUsers();
  } catch (err) { showToast('Error: ' + err.message); }
}

function updateCardVisual() {
  const raw = ($id('pay-card-number')?.value || '').replace(/\D/g, '');
  const last4 = raw.length >= 4 ? raw.slice(-4) : raw.padEnd(4, '—');
  setText('payment-card-display-number', `•••• •••• •••• ${last4}`);
}

function loadSavedCard() {
  const holder = localStorage.getItem('senet_card_holder');
  const number = localStorage.getItem('senet_card_number');
  const expiry = localStorage.getItem('senet_card_expiry');
  const cvv    = localStorage.getItem('senet_card_cvv');
  if (!holder) return;
  const set = (id, val) => { const el = $id(id); if (el) el.value = val; };
  set('pay-card-holder', holder);
  set('pay-card-number', number || '');
  set('pay-expiry',      expiry || '');
  set('pay-cvv',         cvv    || '');
  setText('payment-card-name', holder);
  if (number) {
    const last4 = number.replace(/\D/g, '').slice(-4);
    setText('payment-card-display-number', `•••• •••• •••• ${last4}`);
  }
  if (expiry) setText('payment-card-display-expiry', expiry);
}

async function processPayment() {
  const cardHolder = $id('pay-card-holder')?.value.trim();
  const cardNumber = $id('pay-card-number')?.value.replace(/\s/g, '');
  const expiry = $id('pay-expiry')?.value.trim();
  const cvv = $id('pay-cvv')?.value.trim();
  if (!cardHolder || !cardNumber || !expiry || !cvv) { showToast('Please fill all card details'); return; }
  if (!selectedBookingId) {
    showToast('No active booking. Please book a car first.');
    switchDash('dash-client-cars', $id('nav-client-cars'));
    return;
  }
  const btn = $id('pay-btn');
  if (btn) { btn.textContent = 'Processing...'; btn.disabled = true; }
  try {
    await apiCreatePayment({ bookingId: selectedBookingId, cardNumber: cardNumber.slice(-4), expiry, amount: selectedBookingAmount });
    localStorage.setItem('senet_card_holder', cardHolder);
    localStorage.setItem('senet_card_number', $id('pay-card-number')?.value || '');
    localStorage.setItem('senet_card_expiry', expiry);
    localStorage.setItem('senet_card_cvv',    cvv);
    showToast('Payment received — Awaiting admin confirmation ✦');
    selectedBookingId = null;
    selectedBookingAmount = 0;
    localStorage.removeItem('senet_pending_booking');
    localStorage.removeItem('senet_pending_amount');    
    await renderCars(); 
    switchDash('dash-payments', $id('nav-payments'));
  } catch (err) {
    showToast('Payment failed: ' + err.message);
  } finally {
    if (btn) { btn.textContent = '✦ Pay'; btn.disabled = false; }
  }
}

// ── Page Navigation ───────────────────────────────────────────────
function showPage(id) {
  if (id === 'dashboard' && !getToken()) { id = 'page-login'; }
  pushHistory(id, null);
  document.querySelectorAll('.page, .dashboard-layout').forEach(p => p.classList.remove('active'));
  const target = $id(id);
  if (target) target.classList.add('active');
  window.scrollTo(0, 0);
  if (id === 'dashboard') {
    applyRoleUI();
    renderBookings();
    const role = getRole();
    const saved = localStorage.getItem('senet_last_dash');
    const navMap = {
      'dash-overview':     'nav-overview',
      'dash-cars':         'nav-cars',
      'dash-bookings':     (role === 'ADMIN' || role === 'EMPLOYEE') ? 'nav-bookings' : 'nav-my-bookings',
      'dash-client-cars':  'nav-client-cars',
      'dash-booking-form': (role === 'ADMIN' || role === 'EMPLOYEE') ? null : 'nav-new-booking',
      'dash-add-car':      null,
      'dash-car-detail':   null,
      'dash-profile':      null,
      'dash-payment':      null,
      'dash-payments':     'nav-payments',
      'dash-users':        'nav-users',
    };
    const page = (saved && $id(saved)) ? saved :
      (role === 'ADMIN' || role === 'EMPLOYEE') ? 'dash-overview' : 'dash-client-cars';
    _activateDashPage(page, $id(navMap[page] || ''));
    if (page === 'dash-client-cars') renderClientCars();
    if (page === 'dash-profile') loadProfileData();
  }
}

function _activateDashPage(pageId, navEl) {
  document.querySelectorAll('.dash-page').forEach(p => p.classList.remove('active'));
  const target = $id(pageId);
  if (target) target.classList.add('active');
  setText('dash-title', dashTitles[pageId] || '');
  localStorage.setItem('senet_last_dash', pageId);
  pushHistory('dashboard', pageId);
  if (navEl) {
    document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
    navEl.classList.add('active');
  }
}
function setupDateConstraints() {
    const today = new Date().toISOString().split('T')[0];
    const pickupInput = $id('booking-pickup');
    const returnInput = $id('booking-return');
    if (!pickupInput || !returnInput) return;
    pickupInput.min = today;
    returnInput.min = today;
    pickupInput.addEventListener('change', () => {
        if (pickupInput.value) {
            const nextDay = new Date(pickupInput.value);
            nextDay.setDate(nextDay.getDate() + 1);
            returnInput.min = nextDay.toISOString().split('T')[0];
            if (returnInput.value && returnInput.value <= pickupInput.value) {
                returnInput.value = '';
            }
        }
    });
}

function switchDash(pageId, navEl) {
  const dash = $id('dashboard');
  if (!dash?.classList.contains('active')) {
    if (!getToken()) { showPage('page-login'); return; }
    document.querySelectorAll('.page, .dashboard-layout').forEach(p => p.classList.remove('active'));
    dash.classList.add('active');
    applyRoleUI();
    renderBookings();
  }
  _activateDashPage(pageId, navEl);
  closeSidebar();

  if (pageId === 'dash-car-detail' && window.selectedCarId) {
    const c = CARS.find(car => car.id == window.selectedCarId);
    if (c) {
      const img = $id('car-detail-img');
      if (img) { img.src = c.imageUrl || ''; img.alt = `${c.brand} ${c.model}`; }
      setText('car-detail-name', `${c.brand} ${c.model}`);
      setText('car-detail-meta', `${c.brand} · ${c.category} · ${c.year}`);
      const badge = $id('car-detail-status');
      if (badge) { badge.textContent = c.status; badge.className = `badge ${c.status==='available'?'badge-available':'badge-booked'}`; }
      setText('car-detail-engine', c.engine || 'N/A');
      setText('car-detail-trans', c.transmission || 'Automatic');
      setText('car-detail-year', c.year);
      setText('car-detail-category', c.category);
      setText('car-detail-desc', c.description || 'A highly refined luxury vehicle built for maximum comfort and style.');
      setText('car-detail-price', 'EGP ' + c.pricePerDay.toLocaleString());
      const adminBtns = $id('car-detail-admin-btns');
      const role = getRole();
      if (adminBtns) adminBtns.style.display = (role === 'ADMIN' || role === 'EMPLOYEE') ? 'flex' : 'none';
    }
  }

  if (pageId === 'dash-booking-form') {
    populateBookingCarSelect();
    const savedName = localStorage.getItem('senet_name');
    const nameInput = $id('booking-client-name');
    if (nameInput && savedName && !nameInput.value) nameInput.value = savedName;
    setupDateConstraints();
}

  if (pageId === 'dash-client-cars') renderClientCars();
  if (pageId === 'dash-profile') loadProfileData();
  if (pageId === 'dash-payment') loadSavedCard();
  if (pageId === 'dash-payments') renderPayments();
  if (pageId === 'dash-users') renderUsers();
  if (pageId === 'dash-add-car' && !window.editingCarId) {
    setText('add-car-form-title', 'Add New Vehicle');
    setText('add-car-form-label', 'Fleet Management');
    ['add-car-brand','add-car-model','add-car-year','add-car-price','add-car-engine','add-car-image','add-car-description']
      .forEach(id => { const el = $id(id); if (el) el.value = ''; });
  }
}

function applyRoleUI() {
  const role = getRole();
  const isAdmin = role === 'ADMIN';
  const isEmployee = role === 'EMPLOYEE';

  const adminNav = $id('admin-nav-group');
  const clientNav = $id('client-nav-group');
  const employeeNav = $id('employee-nav-group');

  if (adminNav)    adminNav.style.display    = isAdmin                    ? 'block' : 'none';
  if (clientNav)   clientNav.style.display   = (!isAdmin && !isEmployee)  ? 'block' : 'none';
  if (employeeNav) employeeNav.style.display = isEmployee                 ? 'block' : 'none';

  const addCarBtn = $id('add-car-btn');
  if (addCarBtn) addCarBtn.style.display = isAdmin ? 'flex' : 'none';
  const savedName = localStorage.getItem('senet_name') || '';
  const displayName = savedName || (getUserId() ? 'User' : 'Guest');
  const letter = displayName.charAt(0).toUpperCase();

  setText('sidebar-name', displayName);
  setText('sidebar-role', isAdmin ? 'Admin' : isEmployee ? 'Employee' : 'Client');
  document.querySelectorAll('.user-avatar').forEach(el => el.textContent = letter);
  setText('topbar-user-name', displayName.split(' ')[0]);
  setText('profile-name-header', displayName);
  setText('payment-card-name', displayName);

  const profNameInput = $id('profile-name-input');
  if (profNameInput) profNameInput.value = displayName;

  const savedEmail = localStorage.getItem('senet_email') || '';
  setText('profile-email-header', savedEmail);
  const profEmailInput = $id('profile-email-input');
  if (profEmailInput) profEmailInput.value = savedEmail;
}

function navigate(target) {
  if (target === 'cars') showPage('page-cars');
  else if (target === 'dashboard') showPage('dashboard');
  else showPage('page-home');
}

// ── Auth ──────────────────────────────────────────────────────────
async function loginAction(e) {
  if (e) e.preventDefault();
  const email = $id('login-email')?.value.trim();
  const pass = $id('login-pass')?.value;
  if (!email || !pass) { showToast('Enter email and password'); return; }
  try {
    await apiLogin(email, pass);
    localStorage.setItem('senet_email', email);
    showPage('dashboard');
    showToast('Welcome back ✦');
  } catch (err) {
    showToast('Login failed: ' + err.message);
  }
}

async function registerAction(e) {
  if (e) e.preventDefault();
  const email    = $id('register-email')?.value.trim();
  const pass     = $id('register-pass')?.value;
  const confirm  = $id('register-confirm-pass')?.value;
  const fname    = $id('register-fname')?.value.trim() || '';
  const lname    = $id('register-lname')?.value.trim() || '';
  const name     = (fname + ' ' + lname).trim() || email?.split('@')[0] || 'User';
  try {
    if (!email || !pass)      throw new Error('Email and password required');
    if (pass !== confirm)     throw new Error('Passwords do not match');
    if (pass.length < 6)      throw new Error('Password must be at least 6 characters');
    await apiRegister(email, pass, name);
    localStorage.setItem('senet_email', email);
    showPage('dashboard');
    showToast('Account created ✦');
  } catch (err) {
    showToast('Registration failed: ' + err.message);
  }
}

function logoutAction() {
  apiLogout();
  CARS = []; BOOKINGS = []; selectedBookingId = null; selectedBookingAmount = 0;
  localStorage.removeItem('senet_last_dash');
  localStorage.removeItem('senet_pending_booking');
  localStorage.removeItem('senet_pending_amount');
  showPage('page-home');
  showToast('Logged out successfully');
}

// ── Dashboard Titles ──────────────────────────────────────────────
const dashTitles = {
  'dash-overview':     'Overview',
  'dash-cars':         'Fleet Management',
  'dash-bookings':     'Bookings',
  'dash-add-car':      'Add New Vehicle',
  'dash-car-detail':   'Vehicle Detail',
  'dash-booking-form': 'New Booking',
  'dash-profile':      'My Profile',
  'dash-payment':      'Payment',
  'dash-client-cars':  'Browse Cars',
  'dash-payments':     'Payment History',
  'dash-users':        'User Management',
};

// ── Toast ─────────────────────────────────────────────────────────
let toastTimer;
function showToast(msg) {
  const t = $id('toast');
  if (!t) return;
  t.textContent = '✦  ' + msg;
  t.classList.add('show');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => t.classList.remove('show'), 3000);
}

// ── Mobile Sidebar ────────────────────────────────────────────────
function openSidebar() { $id('dashSidebar')?.classList.add('open'); $id('sidebarOverlay')?.classList.add('visible'); }
function closeSidebar() { $id('dashSidebar')?.classList.remove('open'); $id('sidebarOverlay')?.classList.remove('visible'); }
function toggleMobileMenu() {}

// ── Browser History (back/forward arrow support) ──────────────────
function pushHistory(pageId, dashId) {
  const state = { pageId, dashId };
  const url   = dashId ? `#${dashId}` : `#${pageId}`;
  history.pushState(state, '', url);
}

window.addEventListener('popstate', (event) => {
  if (!event.state) return;
  const { pageId, dashId } = event.state;
  if (pageId === 'dashboard' && dashId) {
    // Re-enter dashboard without pushing new history
    if (!getToken()) { showPage('page-login'); return; }
    document.querySelectorAll('.page, .dashboard-layout').forEach(p => p.classList.remove('active'));
    $id('dashboard')?.classList.add('active');
    applyRoleUI();
    renderBookings();
    _activateDashPage(dashId, null);
  } else {
    document.querySelectorAll('.page, .dashboard-layout').forEach(p => p.classList.remove('active'));
    const target = $id(pageId);
    if (target) target.classList.add('active');
  }
});

// ── Init ──────────────────────────────────────────────────────────
renderCars();
if (getToken()) showPage('dashboard');