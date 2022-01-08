# sqlite-fun
This repo uses SQLite APIs directly instead of an abstraction like Room.
One advantage of this approach is that the app database doesn't know about any DAO concretions -- Room suffers from this problem. 
This advantage is useful for projects that want to isolate tables in feature modules and have them get plugged-in
to an :app database via a DI framework like Dagger.
