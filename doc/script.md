# AN INTRODUCTION TO FUNCTIONAL PROGRAMMING - WITH WEB PROGRAMMING EXAMPLES

WHAT IS FUNCTIONAL PROGRAMMING?
===============================

**FP is programming with functions.** But seriously, not cheating. And that means: writing _pure_ functions, i.e without _side effects_. You look their signatures, and you know almost all about them. The best documentation ever. And sometimes, the signature gives you the only thing the function can do (with polymorphic functions, especially - cf. theorems for free). 

**FP is not ...**. Programming with higher orfer functions (map, filter, reduce, ...), i.e. having functions as first class values, inmutable data, recursion (with tail calls), ... These are common ingredients of FP, but they are not sufficient conditions for a functional programming experience - and some of them are not necessary either. Examples? defining memoize functions with uses vars internally, writing a higher order function with side effects, ... These examples will appear latter.

**More on pure functions: equational reasoning**. Pure functions allow _equational reasoning_: reasoning about the behaviour of our programs in terms of substitutions. This is what _referentially transparent expressions_ are all about. "RT is the essence of FP". FP is not a sequence of instructions, it's a RT expression. It's computing with _values_. Functions have to be transparent, in the sense that they do nothing more than what they say. 

**More on pure functions: descriptive programming**. FP is programming without side effects: writing reading from files, databases, sockets, mutating variable, exceptions, etc. But how can we do something useful without doing side effects? We can't indeed. What we do is to _DESCRIBE_ side effects, and then having a different part of the programm _INTERPRETING_ these descriptions. Prgramming without side effects really means programming leaving side effects _aside_, keeping the bulk of your program _pure_ - i.e., programming using RT expressions. At the end of the universe, the side effects described by your program will be effected or executed. Descriptive programming is something that you can do with any language: not only scala, or haskell, but also Java, Python, Ruby, Javascript, ... 

**Examples: this is FP, this is not FP**. To be used throughout this section. Tiny programs which are and are not RT expressions. Those that not will throw exceptions, mutate variables in place, read from files, launch computations (e.g. Scala Futures), etc. The way to introduce side effects. Our icon for purity: ![Mr. proper](http://www.yofuiaegb.com/wp-content/uploads/2012/10/Mr-Propper.jpg) (properly tuned with lambdas).

HOW DO WE PROGRAM WITH FUNCTIONS?
=================================

**Algebraically.** Choosing _data types_, _functions_ (a.k.a operators, combinators) that operate on those data types, and _laws_ which establish the proper behaviour of those functions. _Algebra_ (def.): "... algebra to mean a collection of functions operating over some data type(s), along with a set of laws specifying relationships between these functions."

**Algebraic design.** An API/Library can be described algebrically, by an algebra that obeys specific laws. This is the shape of a purely functional library. Questions: what data types should we use for our library/API? What primitive functions should we define and what might they mean? What laws/properties should they satisfy? The API thus designed forms an algebra. Algebraic design: representations for data types come next; first, interfaces and laws, and then picking up concrete representatios. 

**FP design as an iterative process**. Pure algebraic design: design the ideal API without concerning ourselves with the representation of data types, i.e. working with pure abstract types. But: The design of the API can also be informed by the choice of representation. Functional design is thus an iterative process. 

### FUNCTIONS

**Composing functions: combinators.** A program is a RT expression made from functions, which are combined thanks to higher-order functions: these functions can be properly called _combinators_. The only way to compose functions is through functions. Higher order functions, which receive functions and return functions. These special functions are called combinators. The most simple combinator: _composition_ of functions. Other combinators for functions: compose, andthen, ... Look to the code! 

**FP design: expressiveness.** Primitive vs. derive functions. Expressiveness: what can be expressed with our primitive operations: the universe of meaning that our primitives engender. What operations can be expressed by our API. Sometimes one operation can be expressed in terms of other, more powerful, primitive. It's not primitive then. Something primitive is not really expressible in terms of other primitive. In some cases, we may want to implement the operation primitively to take advantage of the underlying representation, because of efficiency reasons. Reduce an API to a minimal set of primitive functions is important: to reuse the tricky logic, to remove coupling. Goal: find a minimal, yet expressive, set of primitive functions. API components are generic, reusable components that express or factor out common functionaly. 

**FP design: genericity**. Important to identify generic combinators: refining combinators to their more general form. This leads to generic APIs: functors, monoids, monads, etc. These are generic, but at the same time very expressive. More general in the sense that they are able to cover more use cases. The most general also happens to be the most primitive. Same functions in different domains: same signature and same laws. These generic functions are patterns that cut across domains. Fundamental structures that appear over and over again across multiple domains. They represent patterns, and we give them a name in order to: communicate better, quickly find solutions or reduce the design space, imrove conceptual consitency, reuse code!

**More on pure functions: RT expressions and observability**. RT is a relative notion. Although the representation is not pure, the API can be, if this impure stuff is properly encapsulated. It is important that side effects are relegated until the user calls run ... at the end of the day. Difference between effects and side effects. Local side effects may not be observable thanks to scoping constructs (private[...]), or otherwise. All this is to guarantee that the API remains pure - that laws hold. Because side effects can't be accessed externally.

**FP design: expressiveness and purity.** Any hidden or out of band assumption (which is not published in the signature), in particular side effects, in which this primitive may incur would not allow us to treat the function/component as a black box and hence will hamper composability, and hence expressiveness. 

### DATA TYPES

Data types in pure functional programming are *Algebraic Data Types (ADTs)*.

**FP design: data types.** They are more like the *description* of a computation that can be *interpreted* later (or a *program* than we can *run*) than the container of some value. When side effects are removed from the construction of our data structure, we get a pure data structure. Data types have an API. Data types have a representation. In a pure algebraic approach, data type representations don't matter much. It is the relationship between then that matters, as represented by the functions that relate them according to the laws declared for them. 

**Inmutability.** Data are not mutable at all. They are values, and we use Persistent data structures for efficiently copying with mutation. Examples in the REPL: res0 is still there when we add some element to the list. Mutation is thus represented through streams of values. But we lose identity here. FRP to the rescue here, but better support by the language would be desirable.

**Combinator libraries.** _compose_ is a very general combinator. For specific purposes and data types will end up with special-purpose combinator libraries. Examples: Option, Either, Futures, IO, Predicates, ... with their respective combinators: map, flatmap, fold, ... 

**Fold: a general combinator for ADTs.** The case of fold: special interpreter for lists or foldable structures. Defining functions through other functions applied to the different cases of a data type. Generalising fold as an interpreter of the instructions given by an ADT, and the ADT as defining an instruction set.

**A side note: Church encodings.** Data types are functions in disguise! Don't know if this is very useful for this talk though ... 

### LAWS

**FP design: laws.** Meaningful laws aid reasoning, make the API more usable and prove, reinforces, enable he blackboxed-ness of the API. Laws are the guardian of purity. Choosing laws for API: look to the conceptual model, invent them, look to the implementation.

DSLs 
====

**FP design as DSL design.** When we design a library/API we are designing a little language to express a certain kind of computations.

**Awkwardness in functional programming.**: This might be due to some functional abastraction which has not being identified properly. But also the lack of some syntactic sugar. Example for the former: bimap.

**Our icon for DSLs.** 

![Celia Cruz: ¡¡Azúcar!!"](http://www.cubaeuropa.com/homenaje/discografia/fotos/foto2.jpg)

**On usability.** DSLs does not necessarily improve the expressiveness of the API, but its usability, making its use more pleasant. How? Through syntax and helper functions. Scala features: implicits, default args, named args, apply, extractors, companion objects, def macros, annotation macros, ... 

**On patterns.** Builders, wrappers, decorators (implicit class extensions), ...

FP IN PRACTICE: WEB PROGRAMMING EXAMPLES
========================================

_The purpose of this section is illustrating the FP design process in a familiar domain: Web programming, and using a familiar framework: Play. Ideally, the different concerns exposed above should be somehow exemplified: FP design, ADTs, data types, combinator libraries, side effects/purity, genericity, expressiveness, laws, DSLs, patterns, etc. If the Play framework does not comply with some of these principles and concerns, that would also be good for the purpose of this talk._ 

WHY SHOULD WE PROGRAM FUNCTIONALY?
==================================

Arguably, because we will write better programs: understandable, reusable, modifiable, testable, paralelizable, ... Programs that will evolve.

**Modularity.** "the degree into which a system's components may be separated and recombined"

**Composability.** Side effects or unit signatures hamper compositionality: you can't manipulate runnable objets generically since you always need to know something about their internal behaviour. For example: having map2 start executing its args inmediately requires access to the executor service. It's better if this is abstracted away.

NEXT STEPS IN FP
================

Further topics in FP: type classes, monoids, functors, monads, scala check, streams, ...

REFERENCES
==========

**Scalaz.**

**Shapeless.**

....

**Finagle (Twitter).** Handles execution of service specs declared by the programmer using abstractions like futures, services, filtrrs, ... It implements the runtime. This has been enormously beneficial, freeing the programmer from the tedium of managing threads, queues, resource pools, and resource reclamation, allowing him instead to focus on application semantics.
This achieves a kind of modularity as we separate concerns of program semantics from their execution.
