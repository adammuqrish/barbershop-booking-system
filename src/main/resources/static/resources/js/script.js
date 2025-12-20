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

// active nav click
document.addEventListener("DOMContentLoaded", function() {
	var links = document.querySelectorAll('.lg\\:hover\\:bg-transparent');

	// Get current URL path and extract last segment (e.g. 'profile')
	var currentPath = window.location.pathname;
	var currentPage = currentPath.split("/").pop(); // removes everything before last /

	links.forEach(function(link) {
		var linkHref = link.getAttribute("href"); // e.g. 'profile'

		// Highlight if href matches current page
		if (linkHref === currentPage) {
			link.classList.add('text-yellow-300');
		} else {
			link.classList.remove('text-yellow-300');
		}

		// Optional: still handle highlight before reload (on click)
		link.addEventListener('click', function() {
			links.forEach(function(item) {
				item.classList.remove('text-yellow-300');
			});
			link.classList.add('text-yellow-300');
		});
	});
});

// Desktop Appointment Dropdown Toggle
document.addEventListener('DOMContentLoaded', function() {
	const trigger = document.getElementById('appointment-desktop-trigger');
	const menu = document.getElementById('appointment-dropdown-menu');
	if (trigger && menu) {
		trigger.addEventListener('click', function(e) {
			e.stopPropagation();
			menu.classList.toggle('hidden');
		});
		// Hide dropdown when clicking outside
		document.addEventListener('click', function(e) {
			if (!trigger.contains(e.target) && !menu.contains(e.target)) {
				menu.classList.add('hidden');
			}
		});
	}
});

// Mobile Appointment Dropdown Toggle
document.addEventListener('DOMContentLoaded', function() {
	const mobileBtn = document.querySelector('.dropdown-toggle.md\\:hidden, .dropdown-toggle.md\\:block.md\\:hidden');
	const dropdownMenu = document.getElementById('appointment-dropdown-menu');
	if (mobileBtn && dropdownMenu) {
		mobileBtn.addEventListener('click', function(e) {
			e.stopPropagation();
			dropdownMenu.classList.toggle('hidden');
		});
		// Hide dropdown when clicking outside (mobile)
		document.addEventListener('click', function(e) {
			if (!mobileBtn.contains(e.target) && !dropdownMenu.contains(e.target)) {
				dropdownMenu.classList.add('hidden');
			}
		});
	}
});


// Update the text box when a radio button is selected
document.addEventListener('DOMContentLoaded', function() {
	document.querySelectorAll('input[name="slot"]').forEach(function(radio) {
		radio.addEventListener('change', function() {
			document.getElementById('selected-time').value = this.value;
		});
	});
});

