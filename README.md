<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<!--
  Fragment: noEndDateDisableScript(endDateFormName)
  Single responsibility: wire one "No end date" checkbox to its end-date inputs (disable when checked).
  Included by date-input fragment only when the DATE input is an end-date field (name ends with EndDate).
-->
<th:block th:fragment="noEndDateDisableScript(endDateFormName)">
  <script th:attr="data-end-date-form-name=${endDateFormName}">
    (function() {
      var scriptEl = document.currentScript;
      var endDateFormName = scriptEl && scriptEl.getAttribute('data-end-date-form-name') || '';
      var noEndDateCheckboxName = endDateFormName.replace('EndDate[]', 'NoEndDate[]');
      function syncEndDateDisabled(checkbox) {
        if (!checkbox || checkbox.getAttribute('name') !== noEndDateCheckboxName) return;
        var form = checkbox.form;
        if (!form) return;
        var inputs = form.querySelectorAll('input[name="' + endDateFormName + '"]');
        inputs.forEach(function(inp) { inp.disabled = checkbox.checked; });
      }
      function run() {
        var form = document.getElementById('page-form');
        if (!form) return;
        var checkbox = form.querySelector('input[type="checkbox"][name="' + noEndDateCheckboxName + '"]');
        if (checkbox) syncEndDateDisabled(checkbox);
      }
      if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', run);
      } else {
        run();
      }
      document.addEventListener('change', function(e) {
        if (e.target && e.target.type === 'checkbox' && e.target.getAttribute('name') === noEndDateCheckboxName) {
          syncEndDateDisabled(e.target);
        }
      });
    })();
  </script>
</th:block>
</html>
