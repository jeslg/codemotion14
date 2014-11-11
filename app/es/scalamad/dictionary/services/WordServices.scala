package es.scalamad.dictionary.services

import es.scalamad.dictionary.models._

trait WordServices {

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
