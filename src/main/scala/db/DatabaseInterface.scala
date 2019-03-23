package db

// This class will hold all the logic that connects to the database and executes our queries
// We aren't using encryption for our password in the app, because we aren't storing it anywhere
class DatabaseInterface(val password: String) {
}
