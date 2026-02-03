<!DOCTYPE html>
<html th:lang="${#locale.language}" xmlns:th="http://www.thymeleaf.org">
<div th:fragment="date-input (input, data)"
     class="form-group"
     th:classappend="${!inputData.valid(data)} ? 'form-group--error' : ''"
     th:with="inputData=${data.get(input.name)},
              formInputName=${T(org.codeforamerica.shiba.pages.PageUtils).getFormInputName(input.name)},
              hasError=${!data.isValid() && !inputData.valid(data)},
              hasHelpMessage=${input.helpMessageKey != null},
              needsAriaLabel=${input.needsAriaLabel()},
              noEndDateChecked=${#strings.endsWith(input.name, 'EndDate') and data.get(#strings.substring(input.name, 0, #strings.length(input.name) - 8) + 'NoEndDate') != null and data.get(#strings.substring(input.name, 0, #strings.length(input.name) - 8) + 'NoEndDate').value.contains('true')}">
	 <fieldset class="date-input">
        <div th:replace="~{fragments/form-question-prompt :: formQuestionPrompt(${input})}"></div>
        <p class="text--help">
            <label th:for="${input.name}+'-month'"
                   th:id="${input.name}+'-month-label'"
                   th:text="#{general.month}"></label>
            &nbsp;/&nbsp;
            <label th:for="${input.name}+'-day'"
                   th:id="${input.name}+'-day-label'"
                   th:text="#{general.day}"></label>
            &nbsp;/&nbsp;
            <label th:for="${input.name}+'-year'"
                   th:id="${input.name}+'-year-label'"
                   th:text="#{general.year}"></label>
        </p>
        <input type="text" inputmode="numeric" maxlength="2" class="text-input form-width--2-character dob-input"
               th:id="${input.name}+'-month'" th:name="${formInputName}"
               th:value="${(!inputData.value.isEmpty() and #lists.size(inputData.value) > 0) ? inputData.value[0]: ''}"
               th:placeholder="mm"
               th:disabled="${noEndDateChecked}"
               th:attr="aria-describedby=${hasHelpMessage ? input.name + '-help-message' : ''},
                        aria-labelledby=${needsAriaLabel ? '' : input.name + '-label'},
                        aria-invalid=${hasError}"/>
        &nbsp;/&nbsp;
        <input type="text" inputmode="numeric" maxlength="2" class="text-input form-width--2-character dob-input"
               th:id="${input.name}+'-day'"
               th:name="${formInputName}" th:value="${(!inputData.value.isEmpty() and #lists.size(inputData.value) > 1) ? inputData.value[1]: ''}"
               th:placeholder="dd"
               th:disabled="${noEndDateChecked}"
               th:attr="aria-describedby=${hasHelpMessage ? input.name + '-help-message' : ''},
                        aria-labelledby=${needsAriaLabel ? '' : input.name + '-label'},
                        aria-invalid=${hasError}"/>
        &nbsp;/&nbsp;
        <input type="text" inputmode="numeric" maxlength="4" class="text-input form-width--4-character dob-input"
               th:id="${input.name}+'-year'"
               th:name="${formInputName}" th:value="${(!inputData.value.isEmpty() and #lists.size(inputData.value) > 2) ? inputData.value[2]: ''}"
               th:placeholder="yyyy"
               th:disabled="${noEndDateChecked}"
               th:attr="aria-describedby=${hasHelpMessage ? input.name + '-help-message' : ''},
                        aria-labelledby=${needsAriaLabel ? '' : input.name + '-label'},
                        aria-invalid=${hasError}"/>
    </fieldset>
    <div th:replace="~{fragments/inputErrorFragment :: validationError(${data}, ${input})}"></div>
    <!--
      NO END DATE (multi end-date safe):
      When this DATE input is an "End Date", we wire its 3 inputs (month/day/year) to the
      corresponding "No end date" checkbox so that checking it disables only THIS end date's
      inputs. We scope by this form-group so multiple end dates on the same page each work
      independently. Reusable pattern: one script block per end date; each block only touches
      its own form-group and the checkbox whose name is derived from this input's name.
    -->
    <th:block th:if="${#strings.endsWith(input.name, 'EndDate')}">
      <!-- Pass the No End Date checkbox name to script so we find the right checkbox when many exist on the page -->
      <span style="display:none"
            th:attr="data-no-end-date-checkbox-name=${#strings.contains(formInputName, 'EndDate[]') ? #strings.replace(formInputName, 'EndDate[]', 'NoEndDate[]') : #strings.replace(formInputName, 'EndDate', 'NoEndDate')}"></span>
      <script>
        (function() {
          // Get the script element so we can find the form-group that contains THIS end date block only
          var scriptEl = document.currentScript;
          // Find the nearest parent with class "form-group" (the container for this end date's 3 inputs)
          var formGroup = scriptEl && scriptEl.closest && scriptEl.closest('.form-group');
          // If we are not inside a form-group, do nothing (safety)
          if (!formGroup) return;
          // Find the hidden span that holds the "No end date" checkbox name (set by Thymeleaf)
          var spanEl = formGroup.querySelector('span[data-no-end-date-checkbox-name]');
          // Read the checkbox name (e.g. "socialSecurityNoEndDate[]") so we find the right one when many exist
          var noEndDateCheckboxName = (spanEl && spanEl.getAttribute('data-no-end-date-checkbox-name')) || '';
          // If no name was set, do nothing (safety)
          if (!noEndDateCheckboxName) return;
          // Get only the 3 date inputs (month/day/year) inside THIS form-group (not by form name)
          // This way multiple end dates on the same page each control only their own inputs
          var dateInputs = formGroup.querySelectorAll('input.dob-input');
          // Sync disabled state of THIS block's 3 inputs to the given checkbox (only if it is ours)
          function syncThisEndDateDisabled(checkbox) {
            // Ignore if the checkbox is not the one that controls this end date block
            if (!checkbox || checkbox.getAttribute('name') !== noEndDateCheckboxName) return;
            // Only touch exactly 3 inputs (month, day, year) for safety
            if (dateInputs.length !== 3) return;
            // Set disabled on each of the 3 inputs to match the checkbox checked state
            dateInputs.forEach(function(inp) { inp.disabled = checkbox.checked; });
          }
          // Run once on load to set initial disabled state (e.g. when page is re-displayed with saved data)
          function run() {
            // Get the page form (all end date and checkbox inputs live inside it)
            var form = document.getElementById('page-form');
            if (!form) return;
            // Find the "No end date" checkbox that belongs to THIS end date block by its name
            var checkbox = form.querySelector('input[type="checkbox"][name="' + noEndDateCheckboxName + '"]');
            if (checkbox) syncThisEndDateDisabled(checkbox);
          }
          // If the DOM is still loading, run after it is ready
          if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', run);
          } else {
            run();
          }
          // When any checkbox is clicked, only this block's handler will match (by checkbox name)
          document.addEventListener('change', function(e) {
            if (e.target && e.target.type === 'checkbox' && e.target.getAttribute('name') === noEndDateCheckboxName) {
              syncThisEndDateDisabled(e.target);
            }
          });
        })();
      </script>
    </th:block>
</div>
</html>
