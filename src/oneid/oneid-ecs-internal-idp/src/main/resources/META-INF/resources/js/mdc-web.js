window.addEventListener('DOMContentLoaded', () => {
  // setup: https://m2.material.io/develop/web/getting-started
  // components:
  //    https://m2.material.io/components?platform=web
  //    https://github.com/material-components/material-components-web/tree/v12.0.0/packages

  // MDC init
  document.querySelectorAll('.mdc-text-field').forEach((el) => {
    mdc.textField.MDCTextField.attachTo(el);
  });
  mdc.ripple.MDCRipple.attachTo(document.querySelector('.mdc-button'));
  document.querySelectorAll('.mdc-text-field-helper-text').forEach((el) => {
    mdc.textField.MDCTextFieldHelperText.attachTo(el);
  });
});
