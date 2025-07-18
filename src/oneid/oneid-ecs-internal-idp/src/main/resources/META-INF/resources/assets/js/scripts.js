window.addEventListener('DOMContentLoaded', () => {
  // cancel back button
  const backBtn = document.getElementById('back-button');
  if (backBtn) {
    backBtn.addEventListener('click', (e) => {
      e.preventDefault();
      if (window.history.length > 1) {
        history.back();
      } else {
        window.location.href = '/';
      }
    });
  }

  // Toggle password visibility
  const passwordInput = document.getElementById('password');
  const toggleIcon = document.getElementById('toggle-password-icon');
  if (passwordInput && toggleIcon) {
    toggleIcon.addEventListener('click', () => {
      const isPassword = passwordInput.type === 'password';
      passwordInput.type = isPassword ? 'text' : 'password';
      toggleIcon.textContent = isPassword ? 'visibility' : 'visibility_off';
    });
  }

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

  // form submit loading
  const buttons = document.querySelectorAll("btn-loading-action");
  buttons.forEach(button => {
    button.addEventListener("click", function () {
      const form = button.closest("form");
      if (form && form.checkValidity()) {
        // show loader
        const loader = button.querySelector(".loader");
        if (loader) loader.style.display = "inline-block";
        // disable all buttons
        const btnContainer = button.closest(".btn-loading-action-container");
        if (btnContainer) {
          const allButtons = btnContainer.querySelectorAll("button");
          allButtons.forEach(btn => {
            btn.disabled = true;
            btn.classList.add("btn-disabled");
          });
        }
      }
    });
  });

});

function resetAllLoadingAction() {
  document.querySelectorAll(".btn-loading-action-container button").forEach(button => {
    const loader = button.querySelector(".loader");
    if (loader) loader.style.display = "none";
    button.classList.remove("btn-disabled");
  });
}

// Event used when go back through history
window.addEventListener("pageshow", function () {
  resetAllLoadingAction();
});

