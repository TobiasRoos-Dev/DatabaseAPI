# DatabaseAPI
## Usage

### Use DatabaseAPI
#### Access data
```
String hostname = "hostname";
int port        = 3306;
String database = "database";
String password = "password";
String username = "username";
```
#### Connect to Database
```
// First call (e.q. in main)
Database db = Database.getInstance();
db.set_credentials(hostname, port, database, password, username);
db.connect();
```

#### Get instance of Database
```
Database db = Datbase.getInstance();
```

#### Select
```
Hashmap<Integer, Arraylist> result = 
db.select("*", "table", "id = ?", new Object[]{2}, "ORDER BY id DESC")
```
`> SELECT * FROM table WHERE id = ? ORDER BY id DESC`

`> SELECT * FROM table WHERE id = 2 ORDER BY id DESC`

#### Insert
```
boolean success = 
db.insert("name, lastname, age", "table", new Object[]{"max", "muster", 25})
```
`> INSERT INTO table ("name", "lastname", "age") VALUES (?, ? ,?)`

`> INSERT INTO table ("name", "lastname", "age") VALUES ("max", "muster", 25)`

#### Update
```
boolean success = 
db.update("lastname = ?", "table", "id = ?", new Object[]{"müller", 2})
```
`> UPDATE table SET lastname = ? WHERE id = ?`

`> UPDATE table SET lastname = "müller" WHERE id = 2`

#### Delete
```
boolean success = 
db.delete("table", "id = ?", new Object[]{2})
```
`> DELETE FROM table WHERE id = ?`

`> DELETE FROM table WHERE id = 2`
