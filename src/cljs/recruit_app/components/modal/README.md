### Modal
The modal component will wrap functionality around opening and closing the modal (whether or not to show).
When creating a modal component, a fully qualified `modal-key` (defined in the modal namespace) must be passed
in order to register the subs/events. Once created, there is no need to wrap it in a mechanism that will show or hide it.
It will be hidden by default and can be shown via an event.

### Options
The following options can be passed to the modal component:
- `modal-key`: Fully qualified key defined in modal namespace
- `title`: *Optional* Title of modal
- `body`: *Optional* Collection of components to be displayed vertically
- `action`: *Optional* Map of button attributes to create button to be displayed at bottom of modal
  - `label`: Label of button
  - `on-click`: Event to be fired on button click
- `on-close`: *Optional* Additional callback that will be fired when modal is closed (No need to fire `:close-modal` event in this function)

### Events
To open the modal, fire the open event like this:
```
(rf/dispatch [::modal/open-modal ::modal/email])
```
And to close it, fire the close event:
```
(rf/dispatch [::modal/close-modal ::modal/email])
```