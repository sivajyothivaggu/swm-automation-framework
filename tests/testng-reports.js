/**
 * UI interactions for navigator, panels and method lists.
 * Uses jQuery for DOM manipulation.
 *
 * Improvements and best practices applied:
 * - Use const/let instead of var
 * - Strict equality (===)
 * - Comprehensive error handling and logging
 * - JSDoc comments for public functions
 * - Async/await for promise-like operations
 * - Avoid innerHTML (use textContent / jQuery .text())
 * - Defensive coding and improved readability
 */

/* global $, google, window, document */

(function () {
  'use strict';

  /**
   * Log helper that guards against missing console implementations.
   * @param {'debug'|'info'|'warn'|'error'} level
   * @param {...any} args
   */
  function log(level, ...args) {
    /* eslint-disable no-console */
    if (typeof console !== 'undefined' && console[level]) {
      console[level](...args);
    } else if (typeof console !== 'undefined' && console.log) {
      console.log(...args);
    }
    /* eslint-enable no-console */
  }

  /**
   * Safely get attribute from a jQuery element.
   * @param {jQuery} element
   * @param {string} attr
   * @returns {string|null}
   */
  function safeAttr(element, attr) {
    try {
      if (!element || element.length === 0) {
        return null;
      }
      const value = element.attr(attr);
      return (typeof value === 'undefined' || value === null) ? null : value;
    } catch (err) {
      log('error', 'safeAttr: failed to get attribute', attr, err);
      return null;
    }
  }

  /**
   * Get the panel name from a navigator link element.
   * Looks for data-panel or panel-name attribute.
   * @param {jQuery} $el
   * @returns {string|null}
   */
  function getPanelName($el) {
    try {
      if (!$el || $el.length === 0) {
        return null;
      }
      // Prefer data-panel, then panel-name attribute
      const dataPanel = $el.data('panel');
      if (typeof dataPanel === 'string' && dataPanel.length > 0) {
        return dataPanel;
      }
      const panelAttr = safeAttr($el, 'panel-name') || safeAttr($el, 'data-panel-name');
      return panelAttr;
    } catch (err) {
      log('error', 'getPanelName: error determining panel name', err);
      return null;
    }
  }

  /**
   * Show the named panel and hide other panels.
   * Panels are elements with data-panel-name attributes or id matching the panel name.
   * @param {string} panelName
   */
  function showPanel(panelName) {
    try {
      if (!panelName) {
        log('warn', 'showPanel called with empty panelName');
        return;
      }
      // Hide all known panels
      const $allPanels = $('[data-panel-name], .panel, [id]').filter(function () {
        // Keep only elements that are likely panels (have data-panel-name or .panel)
        const $this = $(this);
        return $this.is('[data-panel-name]') || $this.hasClass('panel') || typeof $this.attr('id') === 'string';
      });

      $allPanels.each(function () {
        try {
          const $p = $(this);
          // Prefer ARIA hiding for better accessibility
          $p.hide();
        } catch (innerErr) {
          log('error', 'showPanel: error hiding panel', innerErr);
        }
      });

      // Find the panel by data-panel-name or id
      let $target = $(`[data-panel-name="${panelName}"]`);
      if ($target.length === 0) {
        $target = $(`#${CSS.escape ? CSS.escape(panelName) : panelName}`);
      }

      if ($target.length === 0) {
        log('warn', 'showPanel: panel not found', panelName);
        return;
      }
      $target.show();
      log('info', 'showPanel: shown panel', panelName);
    } catch (err) {
      log('error', 'showPanel: unexpected error', err);
    }
  }

  /**
   * Toggle the visibility of methods inside a method list container.
   * @param {jQuery} $container - container element that holds .method items
   * @param {boolean} hide - whether to hide (true) or show (false)
   */
  function setMethodsVisibility($container, hide) {
    try {
      if (!$container || $container.length === 0) {
        return;
      }
      const $methods = $container.find('a.method, .method');
      if (hide) {
        $methods.hide();
        $container.data('collapsed', 'true');
      } else {
        $methods.show();
        $container.data('collapsed', 'false');
      }
      log('debug', 'setMethodsVisibility: container', $container, 'hide', hide);
    } catch (err) {
      log('error', 'setMethodsVisibility error', err);
    }
  }

  /**
   * Install handlers for method lists of a given type.
   * Expects markup like <div class="method-list" data-type="failed">...<a class="method">...</a>...</div>
   * @param {string} type - type name to target (e.g., "failed", "skipped", "passed")
   * @param {boolean} [hideByDefault=false] - whether to hide the methods on init
   */
  function installMethodHandlers(type, hideByDefault) {
    try {
      const collapsedDefault = Boolean(hideByDefault);
      const selector = `.method-list[data-type="${type}"], .methods-${type}`;
      const $containers = $(selector);
      if (!$containers || $containers.length === 0) {
        log('warn', 'installMethodHandlers: no containers found for type', type, 'selector', selector);
        return;
      }

      $containers.each(function () {
        try {
          const $container = $(this);

          // Ensure there's a header to attach the toggle
          let $header = $container.find('.method-list-header').first();
          if ($header.length === 0) {
            // Try to find a heading inside container
            $header = $container.find('h2, h3, h4').first();
          }

          // Create a toggle control if absent
          let $toggle = $container.find('.method-toggle').first();
          if ($toggle.length === 0) {
            $toggle = $('<button type="button" class="method-toggle" aria-expanded="true"></button>');
            $toggle.text(collapsedDefault ? 'Show' : 'Hide');
            if ($header.length) {
              $header.append(' ', $toggle);
            } else {
              // Prepend the toggle to container if no header
              $container.prepend($toggle);
            }
          }

          // Initialize collapsed state
          setMethodsVisibility($container, collapsedDefault);
          $toggle.attr('aria-expanded', (!collapsedDefault).toString());
          $toggle.off('click').on('click', function (ev) {
            try {
              ev.preventDefault();
              const isCollapsed = $container.data('collapsed') === 'true';
              const newState = !isCollapsed;
              setMethodsVisibility($container, !newState); // invert: show if newState true
              $toggle.attr('aria-expanded', newState.toString());
              $toggle.text(newState ? 'Hide' : 'Show');
              log('info', 'installMethodHandlers: toggled methods for', type, 'newState', newState);
            } catch (innerErr) {
              log('error', 'installMethodHandlers: toggle click failed', innerErr);
            }
          });
        } catch (err) {
          log('error', 'installMethodHandlers: failed initializing one container', err);
        }
      });
    } catch (err) {
      log('error', 'installMethodHandlers: unexpected error', err);
    }
  }

  /**
   * Fetch method details asynchronously.
   * In a real system this might fetch details from the server. Here we simulate.
   * @param {string} methodId
   * @returns {Promise<{id:string,status:string,message:string,stack?:string}>}
   */
  async function fetchMethodDetails(methodId) {
    try {
      // Simulate async operation - replace with fetch/AJAX if needed
      return await new Promise((resolve) => {
        // Delayed resolution to emulate async behavior
        setTimeout(() => {
          resolve({
            id: methodId,
            status: 'unknown',
            message: `Details for method ${methodId}`,
            stack: null
          });
        }, 10);
      });
    } catch (err) {
      log('error', 'fetchMethodDetails failed for', methodId, err);
      throw err;
    }
  }

  /**
   * Display method details in a details pane.
   * Looks for #method-details or creates a pane at the end of document body.
   * @param {jQuery} $link - anchor or element representing the method
   */
  async function showMethod($link) {
    try {
      if (!$link || $link.length === 0) {
        log('warn', 'showMethod called with empty link');
        return;
      }

      // Extract method id: data-method-id, href fragment, or text
      let methodId = $link.data('method-id') || $link.attr('data-method-id');
      if (!methodId) {
        const href = $link.attr('href') || '';
        // If href is like #method-123 or /#method-123
        const hashMatch = href.match(/#(.+)$/);
        if (hashMatch && hashMatch[1]) {
          methodId = hashMatch[1];
        } else {
          // fallback to text
          methodId = $link.text().trim();
        }
      }

      if (!methodId) {
        log('warn', 'showMethod: could not determine method id for element', $link);
        return;
      }

      // Highlight selection
      try {
        $('.method').removeClass('method-selected');
        $link.addClass('method-selected');
      } catch (err) {
        log('error', 'showMethod: failed to update selection', err);
      }

      // Ensure a details pane exists
      let $details = $('#method-details');
      if ($details.length === 0) {
        $details = $('<section id="method-details" aria-live="polite"></section>');
        // Append with a headline
        const $title = $('<h3></h3>');
        $title.text('Method Details');
        $details.append($title);
        // Append a content container
        const $content = $('<div class="method-details-content"></div>');
        $details.append($content);
        $('body').append($details);
      }

      const $content = $details.find('.method-details-content').first();
      if ($content.length === 0) {
        log('warn', 'showMethod: details content container missing, recreating');
        $details.empty();
        $details.append($('<h3>').text('Method Details'));
        const $newContent = $('<div class="method-details-content"></div>');
        $details.append($newContent);
      }

      // Fetch details asynchronously
      let details;
      try {
        details = await fetchMethodDetails(methodId);
      } catch (fetchErr) {
        log('error', 'showMethod: failed to fetch details for', methodId, fetchErr);
        $details.find('.method-details-content').first().text('Failed to load method details.');
        return;
      }

      // Render details using safe text setting
      try {
        const $contentFinal = $details.find('.method-details-content').first();
        $contentFinal.empty();

        const $id = $('<div class="method-detail-id"></div>').text(`ID: ${details.id}`);
        const $status = $('<div class="method-detail-status"></div>').text(`Status: ${details.status}`);
        const $message = $('<div class="method-detail-message"></div>').text(details.message || '');
        $contentFinal.append($id, $status, $message);

        if (details.stack) {
          // Present stacktrace in a pre element but set text to avoid HTML injection
          const $stack = $('<pre class="method-detail-stack"></pre>');
          $stack.text(details.stack);
          $contentFinal.append($stack);
        }

        log('info', 'showMethod: displayed details for', methodId);
      } catch (renderErr) {
        log('error', 'showMethod: error rendering details', renderErr);
        $details.find('.method-details-content').first().text('An error occurred while displaying details.');
      }
    } catch (err) {
      log('error', 'showMethod: unexpected error', err);
    }
  }

  /**
   * Initialize document-level handlers and setup.
   */
  function initialize() {
    try {
      // Navigator link click handling
      $('a.navigator-link').off('click').on('click', function (event) {
        try {
          event.preventDefault();
          const $this = $(this);

          // Extract the panel for this link
          const panel = getPanelName($this);
          if (!panel) {
            log('warn', 'Navigator link has no panel-name attribute', $this);
            return;
          }

          // Mark this link as currently selected
          try {
            $('.navigator-link').parent().removeClass('navigator-selected');
            $this.parent().addClass('navigator-selected');
          } catch (clsErr) {
            log('error', 'Navigator link selection update failed', clsErr);
          }

          showPanel(panel);
        } catch (innerErr) {
          log('error', 'Error in navigator-link click handler', innerErr);
        }
      });

      // Install handlers for various method groups
      installMethodHandlers('failed');
      installMethodHandlers('skipped');
      installMethodHandlers('passed', true); // hide passed methods by default

      // Method click handling
      $('a.method, .method').off('click').on('click', function (event) {
        try {
          event.preventDefault();
          const $this = $(this);
          // Use async showMethod but do not block the click handler
          showMethod($this).catch((err) => {
            log('error', 'Method click handler failed to show method', err);
          });
        } catch (err) {
          log('error', 'Error in method click handler', err);
        }
      });

      // Global error handlers for uncaught exceptions and unhandled rejections
      window.addEventListener('error', function (ev) {
        try {
          log('error', 'Global error captured', ev && ev.message ? ev.message : ev);
        } catch (err) {
          // ignore
        }
      });

      window.addEventListener('unhandledrejection', function (ev) {
        try {
          log('error', 'Unhandled promise rejection', ev && ev.reason ? ev.reason : ev);
        } catch (err) {
          // ignore
        }
      });

      log('info', 'UI initialization completed');
    } catch (err) {
      log('error', 'initialize: unexpected error during setup', err);
    }
  }

  // Run initialization on document ready
  $(document).ready(function () {
    try {
      initialize();
    } catch (err) {
      log('error', 'Document ready initialization failed', err);
    }
  });
}());