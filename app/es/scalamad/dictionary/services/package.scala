package es.scalamad.dictionary

import es.scalamad.dictionary.models.DictionaryState

package object services {
  type Service[A] = DictionaryState => (A, DictionaryState)
}
