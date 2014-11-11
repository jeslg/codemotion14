package es.scalamad.dictionary.models

object Service {

  type Service[A] = DictionaryState => (A, DictionaryState)

  trait UserService {

    def getUser(nick: String): Service[Option[User]] = { state =>
      (state.users.get(nick), state)
    }

    def addUser(user: User): Service[Unit] = { state =>
      ((), state.copy(users = state.users + (user.nick -> user)))
    }

    def resetUsers(users: User*): Service[Unit] = { state =>
      val m = users.map(u => u.nick -> u).toMap
      ((), state.copy(users = m))
    }
  }

  trait WordService {

    def containsWord(word: String): Service[Boolean] = { state =>
      (state.words.contains(word), state)
    }

    def getWord(word: String): Service[Option[String]] = { state =>
      (state.words.get(word), state)
    }

    def setWord(entry: (String, String)): Service[Unit] = { state =>
      ((), state.copy(words = state.words + entry))
    }

    def resetWords(entries: (String, String)*): Service[Unit] = { state =>
      ((), state.copy(words = entries.toMap))
    }
  }
}
