/**
 * UI interactions for navigator, panels and method lists.
 * Uses jQuery for DOM manipulation.
 *
 * Best practices applied:
 * - Use const/let instead of var
 * - Strict equality (===)
 * - Comprehensive error handling and logging
 * - JSDoc comments for public functions
 * - Improved readability
 */

/* global $, google, window, document */

(function () {
  'use strict';

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
      return element.attr(attr) || null;
    } catch (err) {
      console.error('safeAttr: failed to get attribute', attr, err);
      return null;
    }
  }

  /**
   * Document ready initialization.
   */
  $(document).ready(function () {
    try {
      // Navigator link click handling
      $('a.navigator-link').on('click', function (event) {
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

      installMethodHandlers('failed');
      installMethodHandlers('skipped');
      installMethodHandlers('passed', true); // hide passed methods by default

      $('a.method').on('click', function (event) {
        try {
          event.preventDefault();
          const $this = $(this);
          showMethod($this);
        } catch (err) {
          console.error('Error in method click handler', err);
        }
        return false;
      });

      // Hide all the panels and display the first one (do this last
      // to make sure the click() will invoke the listeners)
      $('.panel').hide();
      $('.navigator-link').first().trigger('click');

      // Collapse/expand the suites
      $('a.collapse-all-link').on('click', function (event) {
        try {
          event.preventDefault();
          const contents = $('.navigator-suite-content');
          if (contents.css('display') === 'none') {
            contents.show();
          } else {
            contents.hide();
          }
        } catch (err) {
          console.error('Error in collapse-all-link handler', err);
        }
      });
    } catch (err) {
      console.error('Initialization error', err);
    }
  });

  /**
   * Install handlers that show/hide methods for a given status.
   *
   * @param {string} name - status name (e.g., 'failed', 'skipped', 'passed')
   * @param {boolean} [hide=false] - if true, hide methods by default
   */
  function installMethodHandlers(name, hide = false) {
    /**
     * Get the content block for the method list based on the triggering element.
     * @param {jQuery} t
     * @returns {jQuery}
     */
    function getContent(t) {
      const panel = safeAttr(t, 'panel-name');
      if (!panel) {
        return $();
      }
      return $(`.method-list-content.${name}.${panel}`);
    }

    /**
     * Get hide link element for the given element and status.
     * @param {jQuery} t
     * @returns {jQuery}
     */
    function getHideLink(t) {
      const panel = safeAttr(t, 'panel-name');
      if (!panel) {
        return $();
      }
      const selector = `a.hide-methods.${name}.${panel}`;
      return $(selector);
    }

    /**
     * Get show link element for the given element and status.
     * @param {jQuery} t
     * @returns {jQuery}
     */
    function getShowLink(t) {
      const panel = safeAttr(t, 'panel-name');
      if (!panel) {
        return $();
      }
      const selector = `a.show-methods.${name}.${panel}`;
      return $(selector);
    }

    /**
     * Get method panel class selection for the element and status.
     * @param {jQuery} element
     * @returns {jQuery}
     */
    function getMethodPanelClassSel(element) {
      const panelName = getPanelName(element);
      if (!panelName) {
        return $();
      }
      const sel = `.${panelName}-class-${name}`;
      return $(sel);
    }

    // Hide link handler
    $(`a.hide-methods.${name}`).on('click', function (event) {
      try {
        event.preventDefault();
        const $this = $(this);
        const w = getContent($this);
        w.hide();
        getHideLink($this).hide();
        getShowLink($this).show();
        getMethodPanelClassSel($this).hide();
        console.debug(`hide-methods.${name} clicked for panel`, safeAttr($this, 'panel-name'));
      } catch (err) {
        console.error(`Error handling hide-methods.${name} click`, err);
      }
      return false;
    });

    // Show link handler
    $(`a.show-methods.${name}`).on('click', function (event) {
      try {
        event.preventDefault();
        const $this = $(this);
        const w = getContent($this);
        w.show();
        getHideLink($this).show();
        getShowLink($this).hide();
        showPanel(getPanelName($this));
        getMethodPanelClassSel($this).show();
        console.debug(`show-methods.${name} clicked for panel`, safeAttr($this, 'panel-name'));
      } catch (err) {
        console.error(`Error handling show-methods.${name} click`, err);
      }
      return false;
    });

    // Trigger initial state
    try {
      if (hide === true) {
        $(`a.hide-methods.${name}`).trigger('click');
      } else {
        $(`a.show-methods.${name}`).trigger('click');
      }
    } catch (err) {
      console.error('Error triggering initial show/hide for', name, err);
    }
  }

  /**
   * Get the hash-for-method attribute for an element.
   * @param {jQuery} element
   * @returns {string|null}
   */
  function getHashForMethod(element) {
    return safeAttr(element, 'hash-for-method');
  }

  /**
   * Get the panel-name attribute for an element.
   * @param {jQuery} element
   * @returns {string|null}
   */
  function getPanelName(element) {
    return safeAttr(element, 'panel-name');
  }

  /**
   * Show a panel by its name.
   * @param {string} panelName
   */
  function showPanel(panelName) {
    try {
      if (!panelName) {
        console.warn('showPanel called without a panelName');
        return;
      }
      $('.panel').hide();
      const panel = $(`.panel[panel-name="${panelName}"]`);
      if (panel && panel.length > 0) {
        panel.show();
      } else {
        console.warn('Panel not found for panelName:', panelName);
      }
    } catch (err) {
      console.error('showPanel error', err);
    }
  }

  /**
   * Show a specific method (navigate to hash and scroll adjustment).
   * @param {jQuery} element
   */
  function showMethod(element) {
    try {
      const hashTag = getHashForMethod(element);
      const panelName = getPanelName(element);
      if (!hashTag) {
        console.warn('showMethod missing hash-for-method attribute', element);
        return;
      }
      if (!panelName) {
        console.warn('showMethod missing panel-name attribute', element);
        return;
      }

      showPanel(panelName);

      // Preserve base URL without hash and update location hash safely
      const current = document.location.href || '';
      const hashIndex = current.indexOf('#');
      const base = hashIndex >= 0 ? current.substring(0, hashIndex) : current;
      // Ensure proper encoding of the hash fragment
      const encodedHash = encodeURIComponent(hashTag);
      document.location.href = `${base}#${encodedHash}`;

      // Adjust scroll so heading isn't hidden behind fixed headers (if any)
      try {
        // Calculate offset: current document scroll - 65 (original behaviour)
        const newPosition = $(document).scrollTop() - 65;
        // Set only if a numeric value
        if (Number.isFinite(newPosition)) {
          $(document).scrollTop(newPosition);
        }
      } catch (scrollErr) {
        console.warn('Failed to adjust scroll position', scrollErr);
      }
    } catch (err) {
      console.error('showMethod error', err);
    }
  }

  /**
   * Draws tables using google.visualization.Table.
   * If suiteTableInitFunctions contains function names, they will be invoked.
   * This function will await if any init function returns a Promise.
   *
   * @returns {Promise<void>}
   */
  async function drawTable() {
    try {
      // Initialize any named table init functions
      if (Array.isArray(window.suiteTableInitFunctions)) {
        for (let i = 0; i < window.suiteTableInitFunctions.length; i++) {
          try {
            const fnName = window.suiteTableInitFunctions[i];
            if (typeof fnName !== 'string') {
              console.warn('Ignoring non-string entry in suiteTableInitFunctions at index', i, fnName);
              continue;
            }
            const fn = window[fnName];
            if (typeof fn === 'function') {
              const result = fn();
              if (result && typeof result.then === 'function') {
                // Await promises to follow async/await best practice
                await result;
              }
            } else {
              console.warn('suiteTableInitFunctions entry not found or not a function:', fnName);
            }
          } catch (innerErr) {
            console.error('Error invoking suiteTableInitFunctions entry at index', i, innerErr);
          }
        }
      }

      // Draw each table in suiteTableData
      if (window.suiteTableData && typeof window.suiteTableData === 'object') {
        const keys = Object.keys(window.suiteTableData);
        for (let idx = 0; idx < keys.length; idx++) {
          const k = keys[idx];
          try {
            const v = window.suiteTableData[k];
            if (!v) {
              continue;
            }
            const div = v.tableDiv;
            const data = v.tableData;
            if (!div) {
              console.warn('Missing tableDiv for suiteTableData key', k);
              continue;
            }
            if (!data) {
              console.warn('Missing tableData for suiteTableData key', k);
              continue;
            }
            const container = document.getElementById(div);
            if (!container) {
              console.warn('Container element not found for id', div);
              continue;
            }
            // Defensive check: google.visualization.Table should exist
            if (!window.google || !window.google.visualization || typeof window.google.visualization.Table !== 'function') {
              console.error('Google Visualization API not available');
              continue;
            }
            const table = new google.visualization.Table(container);
            table.draw(data, {
              showRowNumber: false,
            });
          } catch (innerErr) {
            console.error('Error drawing table for key', k, innerErr);
          }
        }
      } else {
        console.debug('No suiteTableData to draw');
      }
    } catch (err) {
      console.error('drawTable error', err);
    }
  }

  // Expose drawTable in case other scripts call it (preserve original name)
  window.drawTable = drawTable;
})();