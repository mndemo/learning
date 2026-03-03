<!--
  "Prefer not to say" for the applicantRaceAndEthnicity page.
  Behavior:
  - When "Prefer not to say" checked: unchecks all race checkboxes, clears other text, hides follow-up.
  - When "Some other" selected: unchecks all other race checkboxes.
  - When any other option selected (e.g. Asian, White): unchecks "Some other" and clears the other text field.
  - When any race selected: unchecks "Prefer not to say" (mutual exclusivity).
-->
<div th:fragment="applicantPreferNotToSayFragment(input, data)"
     xmlns:th="http://www.thymeleaf.org"
     th:with="
       isChecked=${data.get(input.name) != null and data.get(input.name).value.size>0 and data.get(input.name).value[0]=='true' ? true : false}
     ">

  <div th:replace="~{fragments/inputs/checkbox-input :: checkbox-input(input=${input}, data=${data})}"></div>

  <script th:inline="javascript">
	$(document).ready(function () {
	  // Get the "Prefer not to say" checkbox element
	  // Example: <input name="preferNotToSay[]" /> -> returns that input
	  var preferNotToSayCheckbox = document.getElementsByName('preferNotToSay[]')[0];

	  // Check if it was pre-selected when page loaded (e.g. user returned to edit)
	  // The UI shows selection via class "is-selected" on the parent label
	  var checked = preferNotToSayCheckbox.parentElement.classList.contains('is-selected');

	  // References to sections we'll show/hide
	  // container = the race/ethnicity question block; followupDiv = "write your race" text field area
	  var container = $('.question-with-follow-up');
	  var followupDiv = $('div[id="applicantRaceAndEthnicity-follow-up"]');

	  /**
	   * Unchecks all race/ethnicity options and clears the "other" text field.
	   * Example: user had Asian + "Some other" checked -> all get cleared.
	   */
	  function uncheckAllApplicantRaceAndEthnicity() {
	    // Find all race checkboxes (Asian, White, "Some other", etc.)
	    var checkboxInputs = document.querySelectorAll('input[name="applicantRaceAndEthnicity[]"]');
	    checkboxInputs.forEach(function (input) {
	      input.removeAttribute('checked');
	      input.checked = false;
	      // Remove visual "selected" state from the label wrapper
	      if (input.closest('label')) input.closest('label').classList.remove('is-selected');
	    });
	    var otherTextInput = document.querySelector('input[name="otherRaceOrEthnicity[]"], input[name="otherRaceOrEthnicity"]');
	    if (otherTextInput) { otherTextInput.value = ''; }
	  }

	  // On page load: if "Prefer not to say" was already checked, hide the race options
	  if (checked) {
	    uncheckAllApplicantRaceAndEthnicity();
	    container.hide();
	    followupDiv.hide();
	  }

	  // Listen for changes on "Prefer not to say"
	  preferNotToSayCheckbox.addEventListener('change', function () {
	    var isChecked = this.parentElement.classList.contains('is-selected');
	    if (isChecked) {
	      // User just checked it -> clear all race options and hide them
	      uncheckAllApplicantRaceAndEthnicity();
	      container.hide();
	      followupDiv.hide();
	    } else {
	      // User unchecked it -> show the race options again
	      container.show();
	    }
	  });

	  // Listen for changes on each race/ethnicity checkbox
	  var applicantRaceInputs = document.querySelectorAll('input[name="applicantRaceAndEthnicity[]"]');
	  applicantRaceInputs.forEach(function (raceInput) {
	    raceInput.addEventListener('change', function () {
	      // Determine if this checkbox is now selected (either DOM .checked or label's is-selected class)
	      var isSelected = this.checked || (this.closest('label') && this.closest('label').classList.contains('is-selected'));

	      // Rule: selecting any race option unchecks "Prefer not to say"
	      if (isSelected) {
	        preferNotToSayCheckbox.checked = false;
	        if (preferNotToSayCheckbox.closest('label')) preferNotToSayCheckbox.closest('label').classList.remove('is-selected');
	      }

	      // Rule: selecting "Some other" unchecks all other race checkboxes
	      // Example: user had Asian checked, then checks "Some other" -> Asian gets unchecked
	      if (this.value === 'SOME_OTHER_RACE_OR_ETHNICITY' && isSelected) {
	        applicantRaceInputs.forEach(function (other) {
	          if (other !== raceInput) {
	            other.removeAttribute('checked');
	            other.checked = false;
	            if (other.closest('label')) other.closest('label').classList.remove('is-selected');
	          }
	        });
	      }

	      // Rule: selecting any other option (Asian, White, etc.) unchecks "Some other" and clears its text
	      // Example: user had "Some other" + "My custom race" typed -> both get cleared when they check Asian
	      if (this.value !== 'SOME_OTHER_RACE_OR_ETHNICITY' && isSelected) {
	        applicantRaceInputs.forEach(function (other) {
	          if (other.value === 'SOME_OTHER_RACE_OR_ETHNICITY') {
	            other.removeAttribute('checked');
	            other.checked = false;
	            if (other.closest('label')) other.closest('label').classList.remove('is-selected');
	          }
	        });
	        var otherTextInput = document.querySelector('input[name="otherRaceOrEthnicity[]"], input[name="otherRaceOrEthnicity"]');
	        if (otherTextInput) otherTextInput.value = '';
	      }
	    });
	  });
	});
  </script>
</div>
