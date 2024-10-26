# Contributing
I have very limited resources in terms of handling feedback on my projects. So here are the limitations to keep in mind:
- I mostly look into **small** PRs that suggest
    - bug fixes,
    - documentation fixes.
- On the other hand, PRs that
    - implement new features,
    - refactor/cleanup the code.
- have lower priority for me, because I need to review them and decide if I really want that.

What qualifies as a bug, a feature, or refactoring is entirely upon my interpretation.

If you want something, or you don't like how a particular thing works/is implemented, you may open an issue, and we
will have a discussion here, after which I may allow you to make a contribution. By that I mean that I will
probably merge your PR.

# Code Style
There are some things about my code style that I have got used to:
 - For indentation 4 spaces must be used
 - Every instance field access must be qualified with `this` keyword
 - Every instance method call must be qualified with `this` keyword
 - Every static field access must be qualified with the enclosing class name
 - Every static method call must be qualified with the enclosing class name
 - Do not add `final` when declaring local variables
 - Do not use `var`, we are not programming in JavaScript

If you have question "why?" - doing so makes things more clear.