const sqlite3 = require('sqlite3').verbose();
const db = new sqlite3.Database('./data/users.db');
db.run("UPDATE users SET name = 'Ahmed Hassan' WHERE email = 'admin@senet.com'", function(err) {
  if (err) console.error(err);
  else console.log('Updated rows:', this.changes);
  db.close();
});
