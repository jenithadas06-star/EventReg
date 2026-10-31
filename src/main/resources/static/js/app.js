/* ==========================================================================
   Campus Events - small progressive enhancements
   Everything here is optional: the portal works without JavaScript, the
   server re-validates every rule.
   ========================================================================== */

(function () {
    "use strict";

    // --- Mobile navigation ------------------------------------------------
    document.addEventListener("click", function (event) {
        var toggle = event.target.closest("[data-nav-toggle]");
        if (toggle) {
            var links = document.querySelector(".nav-links");
            if (links) {
                links.classList.toggle("is-open");
            }
        }
    });

    // --- Password visibility toggle ---------------------------------------
    document.addEventListener("click", function (event) {
        var button = event.target.closest("[data-password-toggle]");
        if (!button) {
            return;
        }
        var wrapper = button.closest(".password-wrap");
        var input = wrapper ? wrapper.querySelector("input") : null;
        if (!input) {
            return;
        }
        var showing = input.type === "text";
        input.type = showing ? "password" : "text";
        button.textContent = showing ? "Show" : "Hide";
        button.setAttribute("aria-label", showing ? "Show password" : "Hide password");
    });

    // --- Confirm destructive actions --------------------------------------
    document.addEventListener("submit", function (event) {
        var form = event.target;
        var message = form.getAttribute("data-confirm");
        if (message && !window.confirm(message)) {
            event.preventDefault();
        }
    });

    // --- Question builder (add / remove rows) ------------------------------
    function rowsContainer() {
        return document.querySelector("[data-question-rows]");
    }

    function nextIndex(container) {
        var rows = container.querySelectorAll(".question-row");
        var highest = -1;
        rows.forEach(function (row) {
            var index = parseInt(row.getAttribute("data-index"), 10);
            if (!isNaN(index) && index > highest) {
                highest = index;
            }
        });
        return highest + 1;
    }

    function renumber(container) {
        container.querySelectorAll(".question-row").forEach(function (row, position) {
            var label = row.querySelector(".question-row__index");
            if (label) {
                label.textContent = "Question " + (position + 1);
            }
        });
        var counter = document.querySelector("[data-question-count]");
        if (counter) {
            counter.textContent = container.querySelectorAll(".question-row").length;
        }
    }

    function toggleOptions(row) {
        var select = row.querySelector("[data-question-type]");
        var options = row.querySelector("[data-question-options]");
        if (!select || !options) {
            return;
        }
        options.style.display = select.value === "MULTIPLE_CHOICE" ? "" : "none";
    }

    document.addEventListener("change", function (event) {
        if (event.target.matches("[data-question-type]")) {
            toggleOptions(event.target.closest(".question-row"));
        }
    });

    document.addEventListener("click", function (event) {
        if (event.target.closest("[data-add-question]")) {
            var container = rowsContainer();
            var template = document.getElementById("question-row-template");
            if (!container || !template) {
                return;
            }
            var index = nextIndex(container);
            var html = template.innerHTML.split("__INDEX__").join(index);
            var holder = document.createElement("div");
            holder.innerHTML = html;
            var row = holder.firstElementChild;
            container.appendChild(row);
            toggleOptions(row);
            renumber(container);
            var firstInput = row.querySelector("input[type=text]");
            if (firstInput) {
                firstInput.focus();
            }
            return;
        }

        var remove = event.target.closest("[data-remove-question]");
        if (remove) {
            var row = remove.closest(".question-row");
            var container = rowsContainer();
            if (row && container) {
                row.remove();
                renumber(container);
            }
        }
    });

    document.addEventListener("DOMContentLoaded", function () {
        document.querySelectorAll(".question-row").forEach(toggleOptions);
        var container = rowsContainer();
        if (container) {
            renumber(container);
        }
        initPreview();
    });

    // --- Live event preview on the create / edit form ----------------------
    function value(id) {
        var element = document.getElementById(id);
        return element && element.value ? element.value : "";
    }

    function text(id, fallback) {
        var element = document.querySelector('[data-preview="' + id + '"]');
        if (!element) {
            return;
        }
        var raw = value(id);
        element.textContent = raw ? raw : (fallback || "-");
    }

    function initPreview() {
        var panel = document.querySelector("[data-preview-panel]");
        if (!panel) {
            return;
        }
        var form = document.getElementById("event-form");
        if (!form) {
            return;
        }

        function refresh() {
            text("name", "Event name");
            text("category", "Category");
            text("venue", "Venue");
            text("eventDate", "Date");
            text("startTime", "Time");
            text("maxParticipants", "0");

            var title = panel.querySelector('[data-preview="name"]');
            if (title) {
                title.textContent = value("name") || "Event name";
            }
        }

        form.addEventListener("input", refresh);
        form.addEventListener("change", refresh);
        refresh();
    }

    // --- Poster image fallback --------------------------------------------
    document.addEventListener("error", function (event) {
        var image = event.target;
        if (image.tagName === "IMG" && image.hasAttribute("data-poster")) {
            image.style.display = "none";
            var fallback = image.parentElement && image.parentElement.querySelector(".poster-fallback");
            if (fallback) {
                fallback.style.display = "grid";
            }
        }
    }, true);
})();
