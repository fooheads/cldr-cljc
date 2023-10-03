# cldr-cljc

A Clojure library build on top of Unicode CLDR (Common Locale Data
Repository) https://cldr.unicode.org/.

The intention is not be dependant on Java and Javascript libraries for
locale specific formatting, and to be able to fine-tune build sizes from
actual project needs.

The initial version of the library only have the generation part and
a minimal feature set to retreive CLDR data, but formatting and similar
things will follow.

