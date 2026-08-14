document.addEventListener('DOMContentLoaded', function() {
	// Get references to the button and the menu
	const button = document.getElementById('menu-button');
	const menu = document.getElementById('navbar-default');

	// Add a click event listener to the button
	button.addEventListener('click', function(event) {
		// Toggle the 'hidden' class to show/hide the menu
		menu.classList.toggle('hidden');

		// Toggle aria-expanded attribute
		const isExpanded = menu.getAttribute('aria-expanded') === 'true';
		menu.setAttribute('aria-expanded', isExpanded ? 'false' : 'true');

		// Close the menu when clicking outside of it
		if (!isExpanded) {
			document.addEventListener('click', closeMenuOnClickOutside);
		} else {
			document.removeEventListener('click', closeMenuOnClickOutside);
		}

		event.stopPropagation();
	});

	// Function to close the menu when clicking outside of it
	function closeMenuOnClickOutside(event) {
		if (!menu.contains(event.target) && event.target !== button) {
			menu.classList.add('hidden');
			menu.setAttribute('aria-expanded', 'false');
			document.removeEventListener('click', closeMenuOnClickOutside);
		}
	}
});

// nav id link
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
	anchor.addEventListener('click', function(e) {
		e.preventDefault();

		const targetId = this.getAttribute('href').substring(1);
		const targetElement = document.getElementById(targetId);

		if (targetElement) {
			targetElement.scrollIntoView({
				behavior: 'smooth'
			});
		}
	});
});

// previous page function
function goBack() {
	window.history.back(); // This navigates back to the previous page in the browser's history.
}

// Active nav link highlighting (compare normalized paths; covers dropdown sub-items too)
document.addEventListener('DOMContentLoaded', function() {
	var currentPath = window.location.pathname;
	var navLinks = document.querySelectorAll('#navbar-default a[href]');

	navLinks.forEach(function(link) {
		var linkPath;
		try {
			linkPath = new URL(link.href).pathname;
		} catch (e) {
			return; // skip malformed hrefs
		}

		// Normalize: ignore trailing slashes so '/', '' and '/index' behave predictably
		var normalizedLink = linkPath.replace(/\/+$/, '') || '/';
		var normalizedCurrent = currentPath.replace(/\/+$/, '') || '/';

		if (normalizedLink === normalizedCurrent) {
			link.classList.add('text-yellow-300');
		} else {
			link.classList.remove('text-yellow-300');
		}
	});
});

// Generic dropdown toggles driven by [data-dropdown] (works for both desktop and mobile).
// A single delegated handler replaces the previous per-trigger listeners and the fragile
// `.dropdown-toggle.md\:hidden` selector. Only one dropdown is open at a time.
document.addEventListener('DOMContentLoaded', function () {
	const triggers = document.querySelectorAll('[data-dropdown]');

	function closeAllDropdowns(exceptId) {
		triggers.forEach(function (trigger) {
			const menu = document.getElementById(trigger.getAttribute('data-dropdown'));
			if (menu && menu.id !== exceptId) {
				menu.classList.add('hidden');
				trigger.setAttribute('aria-expanded', 'false');
			}
		});
	}

	triggers.forEach(function (trigger) {
		const menu = document.getElementById(trigger.getAttribute('data-dropdown'));
		if (!menu) return;

		trigger.addEventListener('click', function (e) {
			e.stopPropagation();
			const isHidden = menu.classList.contains('hidden');
			closeAllDropdowns(menu.id);
			menu.classList.toggle('hidden', !isHidden);
			trigger.setAttribute('aria-expanded', isHidden ? 'true' : 'false');
		});
	});

	// Close all dropdowns when clicking outside any trigger or menu
	document.addEventListener('click', function (e) {
		let clickedInside = false;
		triggers.forEach(function (trigger) {
			const menu = document.getElementById(trigger.getAttribute('data-dropdown'));
			if (trigger.contains(e.target) || (menu && menu.contains(e.target))) {
				clickedInside = true;
			}
		});
		if (!clickedInside) {
			closeAllDropdowns(null);
		}
	});

	// Close a dropdown immediately when one of its links is clicked, so the menu
	// doesn't stay visible while the browser navigates to the next page
	document.addEventListener('click', function (e) {
		const link = e.target.closest ? e.target.closest('a[href]') : null;
		if (!link) return;

		triggers.forEach(function (trigger) {
			const menu = document.getElementById(trigger.getAttribute('data-dropdown'));
			if (menu && menu.contains(link) && !menu.classList.contains('hidden')) {
				menu.classList.add('hidden');
				trigger.setAttribute('aria-expanded', 'false');
			}
		});
	});
});


// Update the text box when a radio button is selected
document.addEventListener('DOMContentLoaded', function() {
	document.querySelectorAll('input[name="slot"]').forEach(function(radio) {
		radio.addEventListener('change', function() {
			document.getElementById('selected-time').value = this.value;
		});
	});
});

