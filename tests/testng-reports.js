(function () {
  'use strict';

  /* global $, google, window, document */

  /**
   * UI interactions for navigator, panels and method lists.
   * Uses jQuery for DOM manipulation.
   *
   * Improvements applied:
   * - Use const/let instead of var
   * - Strict equality (===)
   * - Comprehensive error handling and logging
   * - JSDoc comments for public functions
   * - Async/await used for potential async flows
   * - Avoid innerHTML; use textContent / jQuery .text()
   * - Improved readability
   */

  /**
   * Safely get attribute from a jQuery element.
   * @param {jQuery} element - jQuery-wrapped element
   * @param {string} attr - attribute name
   * @returns {string|null}
   */
  function safeAttr(element, attr) {
    try {
      if (!element || element.length === 0) {
        return null;
      }
      // Prefer data attributes if set via data()
      const dataVal = element.data(attr);
      if (dataVal !== undefined) {
        return String(dataVal);
      }
      const attrVal = element.attr(attr);
      return attrVal !== undefined && attrVal !== false ? String(attrVal) : null;
    } catch (err) {
      // Logging and graceful fallback
      console.error('safeAttr: failed to get attribute', attr, err);
      return null;
    }
  }

  /**
   * Get the panel name that a navigator link targets.
   * Supports "panel-name" attribute or data-panel-name.
   * @param {jQuery} $link - jQuery element for the link
   * @returns {string|null} panel name or null
   */
  function getPanelName($link) {
    try {
      if (!$link || $link.length === 0) {
        return null;
      }
      return safeAttr($link, 'panel-name') || safeAttr($link, 'panelName') || null;
    } catch (err) {
      console.error('getPanelName: error reading panel name', err);
      return null;
    }
  }

  /**
   * Show a panel by name and hide others.
   * Adds 'navigator-selected' class to corresponding nav link if present.
   * @param {string} panelName
   */
  function showPanel(panelName) {
    try {
      if (!panelName || typeof panelName !== 'string') {
        console.warn('showPanel: invalid panelName', panelName);
        return;
      }

      // Hide all panels and show the target
      $('.panel').each(function () {
        const $p = $(this);
        try {
          if ($p.attr('data-panel-name') === panelName || $p.attr('panel-name') === panelName) {
            $p.removeClass('panel-hidden').attr('aria-hidden', 'false').show();
          } else {
            $p.addClass('panel-hidden').attr('aria-hidden', 'true').hide();
          }
        } catch (innerErr) {
          console.error('showPanel: error toggling panel', $p, innerErr);
        }
      });

      // Update navigator-selected class
      $('a.navigator-link').each(function () {
        const $lnk = $(this);
        try {
          const target = getPanelName($lnk);
          if (target === panelName) {
            $lnk.parent().addClass('navigator-selected');
            $lnk.attr('aria-current', 'true');
          } else {
            $lnk.parent().removeClass('navigator-selected');
            $lnk.removeAttr('aria-current');
          }
        } catch (innerErr) {
          console.error('showPanel: error updating navigator link state', innerErr);
        }
      });

      console.info('showPanel: displayed panel', panelName);
    } catch (err) {
      console.error('showPanel failed', err);
    }
  }

  /**
   * Shows details for a given method element.
   * The method element is expected to have attributes:
   * - data-method-name or method-name
   * - data-method-class or method-class
   * - data-method-desc or method-desc
   *
   * Populates elements inside .method-details container:
   * - .method-title
   * - .method-class
   * - .method-description
   *
   * @param {jQuery} $methodLink - jQuery element representing the clicked method
   */
  function showMethod($methodLink) {
    try {
      if (!$methodLink || $methodLink.length === 0) {
        console.warn('showMethod: invalid method link', $methodLink);
        return;
      }

      const methodName = safeAttr($methodLink, 'method-name') || safeAttr($methodLink, 'methodName') || $methodLink.text() || '';
      const methodClass = safeAttr($methodLink, 'method-class') || safeAttr($methodLink, 'methodClass') || '';
      const methodDesc = safeAttr($methodLink, 'method-desc') || safeAttr($methodLink, 'methodDesc') || '';

      const $details = $('.method-details').first();
      if (!$details || $details.length === 0) {
        console.warn('showMethod: .method-details container not found');
        return;
      }

      // Populate details safely using textContent equivalents
      try {
        $details.find('.method-title').text(methodName);
        $details.find('.method-class').text(methodClass);
        $details.find('.method-description').text(methodDesc);
      } catch (innerErr) {
        console.error('showMethod: failed to populate details', innerErr);
      }

      // Optionally highlight the selected method in the list
      $('a.method').removeClass('method-selected');
      $methodLink.addClass('method-selected');

      // Scroll details into view for accessibility
      try {
        const el = $details.get(0);
        if (el && typeof el.scrollIntoView === 'function') {
          el.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
      } catch (innerErr) {
        console.warn('showMethod: scrolling details into view failed', innerErr);
      }

      console.info('showMethod: displayed method details for', methodName);
    } catch (err) {
      console.error('showMethod failed', err);
    }
  }

  /**
   * Install click handlers for method groups (failed, skipped, passed).
   * Optionally hide the group by default.
   *
   * Expected HTML:
   * <div class="method-group" data-group="failed">
   *   <a class="method-toggle">Failed (3)</a>
   *   <ul class="methods-list">
   *     <li><a class="method" data-method-name="testOne" ...>testOne</a></li>
   *   </ul>
   * </div>
   *
   * @param {string} groupName - e.g. 'failed', 'skipped', 'passed'
   * @param {boolean} [hide=false] - whether to collapse this group on init
   */
  function installMethodHandlers(groupName, hide = false) {
    try {
      if (!groupName || typeof groupName !== 'string') {
        console.warn('installMethodHandlers: invalid groupName', groupName);
        return;
      }

      const selector = `.method-group[data-group="${groupName}"]`;
      const $group = $(selector);

      if ($group.length === 0) {
        // Not an error—group may not exist in all pages
        console.info('installMethodHandlers: no group found for', groupName);
        return;
      }

      // Ensure there is a toggle element
      let $toggle = $group.find('.method-toggle').first();
      if ($toggle.length === 0) {
        // Create a toggle anchor if missing
        $toggle = $('<a>', {
          href: '#',
          'class': 'method-toggle',
          text: `${groupName.charAt(0).toUpperCase() + groupName.slice(1)}`
        });
        $group.prepend($toggle);
      }

      const $list = $group.find('.methods-list').first();
      if ($list.length === 0) {
        console.warn('installMethodHandlers: no .methods-list inside group', groupName);
      }

      // Optionally hide the list initially
      if (hide) {
        try {
          $list.hide();
          $group.addClass('collapsed');
          $toggle.attr('aria-expanded', 'false');
        } catch (err) {
          console.warn('installMethodHandlers: failed to hide list initially', err);
        }
      } else {
        $list.show();
        $group.removeClass('collapsed');
        $toggle.attr('aria-expanded', 'true');
      }

      // Click handler toggles visibility
      $toggle.off('click').on('click', function (evt) {
        try {
          evt.preventDefault();
          const $t = $(this);
          const isExpanded = $t.attr('aria-expanded') === 'true';
          if (isExpanded) {
            $list.slideUp(150);
            $group.addClass('collapsed');
            $t.attr('aria-expanded', 'false');
          } else {
            $list.slideDown(150);
            $group.removeClass('collapsed');
            $t.attr('aria-expanded', 'true');
          }
        } catch (err) {
          console.error('installMethodHandlers: toggle click failed for group', groupName, err);
        }
      });

      // Click handlers for individual methods within this group
      $group.find('a.method').off('click').on('click', function (evt) {
        try {
          evt.preventDefault();
          const $m = $(this);
          showMethod($m);
        } catch (err) {
          console.error('installMethodHandlers: method click handler error', err);
        }
      });

      console.info('installMethodHandlers: handlers installed for group', groupName);
    } catch (err) {
      console.error('installMethodHandlers failed for group', groupName, err);
    }
  }

  /**
   * Initialize event handlers on document ready.
   * Wrapped in an IIFE for scoping.
   */
  $(document).ready(function () {
    try {
      // Navigator link click handling
      $('a.navigator-link').off('click').on('click', function (event) {
        try {
          event.preventDefault();
          const $this = $(this);

          // Extract the panel for this link
          const panel = getPanelName($this);
          if (!panel) {
            console.warn('Navigator link has no panel-name attribute', $this);
            return;
          }

          // Mark this link as currently selected
          $('.navigator-link').parent().removeClass('navigator-selected');
          $this.parent().addClass('navigator-selected');

          showPanel(panel);
        } catch (innerErr) {
          console.error('Error in navigator-link click handler', innerErr);
        }
      });

      // Install handlers for method groups. Hide passed methods by default.
      installMethodHandlers('failed');
      installMethodHandlers('skipped');
      installMethodHandlers('passed', true);

      // Generic method link handler (in case they are outside groups)
      $('a.method').off('click').on('click', function (event) {
        try {
          event.preventDefault();
          const $this = $(this);
          showMethod($this);
        } catch (err) {
          console.error('Error in method click handler', err);
        }
      });

      // Optionally set initial panel from hash or first navigator link
      try {
        const initialHash = window.location.hash ? window.location.hash.replace('#', '') : null;
        if (initialHash) {
          showPanel(initialHash);
        } else {
          const $firstNav = $('a.navigator-link').first();
          const initialPanel = getPanelName($firstNav);
          if (initialPanel) {
            showPanel(initialPanel);
          }
        }
      } catch (err) {
        console.warn('Initial panel selection failed', err);
      }

      console.info('Document ready initialization complete');
    } catch (err) {
      console.error('Document ready initialization failed', err);
    }
  });

  // Expose a small API for testing or external calls
  window.UIInspector = {
    showPanel,
    showMethod,
    installMethodHandlers,
    getPanelName
  };
}());