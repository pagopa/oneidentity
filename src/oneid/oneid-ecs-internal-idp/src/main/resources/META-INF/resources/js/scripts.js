window.addEventListener('DOMContentLoaded', () => {
  // cancel back button
  document.getElementById('back-button').addEventListener('click', (e) => {
    e.preventDefault();
    if (window.history.length > 1) {
      history.back();
    } else {
      window.location.href = '/';
    }
  });

  // Toggle password visibility
  const passwordInput = document.getElementById('password');
  const toggleIcon = document.getElementById('toggle-password-icon');

  toggleIcon.addEventListener('click', () => {
    const isPassword = passwordInput.type === 'password';
    passwordInput.type = isPassword ? 'text' : 'password';
    toggleIcon.textContent = isPassword ? 'visibility' : 'visibility_off';
  });

  // custom input validation (login)
  document.querySelectorAll('.mdc-text-field__input.custom-validation').forEach((input) => {
    const container = input.closest('.input-container');
    const labelContainer = container.querySelector('.mdc-text-field');

    input.addEventListener('invalid', (e) => {
      e.preventDefault();
      labelContainer.classList.add('mdc-text-field--invalid');
    });

    input.addEventListener('input', () => {
      if (input.validity.valid) {
        labelContainer.classList.remove('mdc-text-field--invalid');
      }
    });
  });
});

