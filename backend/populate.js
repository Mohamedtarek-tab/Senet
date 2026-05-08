const fs = require('fs');

async function populate() {
  // 1. Get Admin Token and User ID
  let res = await fetch('http://127.0.0.1:8081/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email: 'admin@senet.com', password: 'admin123' })
  });
  let data = await res.json();
  const token = data.accessToken;
  const adminId = data.userId;
  console.log("Admin logged in. ID:", adminId);

  // 2. Add Cars directly to car-service (bypassing gateway, faking header)
  const cars = [
    { brand:'BMW', model:'7 Series', year:2024, pricePerDay:2400, category:'Luxury Sedan', engine:'3.0L I6', transmission:'Automatic', imageUrl:'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=600&q=70', status:'available', description:'Elegant and technologically advanced luxury sedan.' },
    { brand:'Mercedes-Benz', model:'S-Class', year:2024, pricePerDay:2700, category:'Luxury Sedan', engine:'3.0L I6', transmission:'Automatic', imageUrl:'https://images.unsplash.com/photo-1544636331-e26879cd4d9b?w=600&q=70', status:'available', description:'The ultimate standard in executive transportation.' },
    { brand:'Porsche', model:'Cayenne', year:2023, pricePerDay:3200, category:'SUV', engine:'2.9L V6T', transmission:'Automatic', imageUrl:'https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=600&q=70', status:'available', description:'Sporty driving dynamics meets SUV practicality.' }
  ];

  let addedCars = [];
  for (let car of cars) {
    res = await fetch('http://127.0.0.1:8082/api/cars', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'X-User-Role': 'ADMIN' },
      body: JSON.stringify(car)
    });
    addedCars.push(await res.json());
  }
  console.log(`Added ${addedCars.length} cars.`);

  // 3. Add Bookings directly to booking-service
  const bookings = [
    { car: 'BMW 7 Series', carId: addedCars[0].id, client: 'Admin User', pickup: '2026-06-10', ret: '2026-06-15', amount: 'EGP 12,000', status: 'confirmed' },
    { car: 'Mercedes-Benz S-Class', carId: addedCars[1].id, client: 'Admin User', pickup: '2026-06-20', ret: '2026-06-25', amount: 'EGP 13,500', status: 'pending' }
  ];

  for (let b of bookings) {
    res = await fetch('http://127.0.0.1:8083/api/bookings', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'X-User-Id': adminId },
      body: JSON.stringify(b)
    });
    await res.json();
  }
  console.log("Added bookings.");
}

populate().catch(console.error);
