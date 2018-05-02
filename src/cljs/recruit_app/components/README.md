### Components
This namespace comprises all of the frontend components used on site. All components
should come with their own specific styling but should not necessarily dictate
positioning or layout (except for layout components which are meant to wrap
other components to dictate how they should be laid out on a page).

### Styles
Components use `stylefy` in order to inject styles directly into the element.
Styles should be defined in the `recruit-app.styles` [namespace](https://github.com/TheLadders/recruit-app/blob/master/src/cljs/recruit_app/styles.cljs)
and they can then be referenced in a component using `use-style` from `stylefy`.
`use-style` will return a map of `class` and `style` which can either be used
on an html element directly:
```
[:div (use-style styles/div) "Div Content"]
```
or can be used with a `re-com` component using this [util](https://github.com/TheLadders/recruit-app/blob/master/src/cljs/recruit_app/components/util.cljs#L4-L16):
```
(defn hyperlink
  "Renders re-com hyperlink with proper styles"
  [& params]
  [util/recom-component-with-styles rc/hyperlink params styles/hyperlink])
```

### Layout
Layout components consist of different row and column components that wrap
`h-box` and `v-box` re-com components, respectively. The only difference is
that a row and column component allows for `padding` to be sent in to signify
how much space to put around it. Rows allow for `padding` (to give the same padding
to both the top and bottom), `padding-top` and `padding-bottom` options. Columns
allow for `padding`, `padding-left` and `padding-right` options. These are all
defaulted to 9 currently (which was decided as the logical default). There are also
`row-top`/`row-bottom` and `col-left`/`col-right` components that will only allow
for padding on one side.

### Registered Components
Some components will register subscriptions and events that will be localized
to the given component. For these components, a fully qualified key from the
component namespace must be passed to the component in order to register the
subs/events using that key (and so that events can be triggered from outside
the component).

- [Table](https://github.com/TheLadders/recruit-app/tree/master/src/cljs/recruit_app/components/table)
- [Modal](https://github.com/TheLadders/recruit-app/tree/master/src/cljs/recruit_app/components/modal)
