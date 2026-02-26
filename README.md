<!--
  This fragment implements the "prefer_not_to_say" checkbox for the householdRaceAndEthnicity page.
  When checked, it deselects all other checkboxes and hides follow-ups.
-->

<div th:fragment="preferNotToSayFragment(input, data)"
     th:with="
       isChecked=${data.get(input.name) != null and data.get(input.name).value.size>0 and data.get(input.name).value[0]=='true' ? true : false}
     ">

  <!-- The prefer_not_to_say checkbox -->
  <div th:replace="~{fragments/inputs/checkbox-input :: checkbox-input(input=${input}, data=${data})}"></div>

  <script th:inline="javascript">
	$(document).ready(function () {
	  var checkbox = document.getElementsByName('preferNotToSay[]')[0];
	  var checked = checkbox.parentElement.classList.contains('is-selected');

	  // Target the question-with-follow-up wrapper that contains the race checkboxes
	  var container = $('.question-with-follow-up');
	  var followupDiv = $('div[id="householdRaceAndEthnicity-follow-up"]');

	  if (checked) {
	    container.hide();
	    followupDiv.hide();
	  }

	  // Selectors fixed to match actual rendered HTML
	  const checkboxLabels = document.querySelectorAll('label[for^="AMERICAN"], label[for^="ASIAN"], label[for^="BLACK"], label[for^="HISPANIC"], label[for^="MIDDLE"], label[for^="NATIVE"], label[for^="WHITE"], label[for^="SOME"]');
	  const checkboxInputs = document.querySelectorAll('input[name="householdRaceAndEthnicity[]"]');

	  // Add the missing change listener on preferNotToSay checkbox
	  checkbox.addEventListener('change', function () {
	    var isChecked = this.parentElement.classList.contains('is-selected');
	    if (isChecked) {
	      // Uncheck all race/ethnicity checkboxes
	      checkboxInputs.forEach(function (input) {
	        input.removeAttribute('checked');
	        input.checked = false;
	        input.closest('label').classList.remove('is-selected');
	      });
	      container.hide();
	      followupDiv.hide();
	    } else {
	      container.show();
	    }
	  });
	  });
  </script>
</div>

<!--
  Same as preferNotToSayFragment but for the applicantRaceAndEthnicity page.
  Targets applicantRaceAndEthnicity-follow-up and input[name="applicantRaceAndEthnicity[]"].
-->
<div th:fragment="applicantPreferNotToSayFragment(input, data)"
     th:with="
       isChecked=${data.get(input.name) != null and data.get(input.name).value.size>0 and data.get(input.name).value[0]=='true' ? true : false}
     ">

  <div th:replace="~{fragments/inputs/checkbox-input :: checkbox-input(input=${input}, data=${data})}"></div>

  <script th:inline="javascript">
	$(document).ready(function () {
	  var preferNotToSayCheckbox = document.getElementsByName('preferNotToSay[]')[0];
	  var checked = preferNotToSayCheckbox.parentElement.classList.contains('is-selected');

	  var container = $('.question-with-follow-up');
	  var followupDiv = $('div[id="applicantRaceAndEthnicity-follow-up"]');

	  function uncheckAllApplicantRaceAndEthnicity() {
	    var checkboxInputs = document.querySelectorAll('input[name="applicantRaceAndEthnicity[]"]');
	    checkboxInputs.forEach(function (input) {
	      input.removeAttribute('checked');
	      input.checked = false;
	      if (input.closest('label')) input.closest('label').classList.remove('is-selected');
	    });
	    var otherTextInput = document.querySelector('input[name="otherRaceOrEthnicity[]"], input[name="otherRaceOrEthnicity"]');
	    if (otherTextInput) { otherTextInput.value = ''; }
	  }

	  if (checked) {
	    uncheckAllApplicantRaceAndEthnicity();
	    container.hide();
	    followupDiv.hide();
	  }

	  preferNotToSayCheckbox.addEventListener('change', function () {
	    var isChecked = this.parentElement.classList.contains('is-selected');
	    if (isChecked) {
	      uncheckAllApplicantRaceAndEthnicity();
	      container.hide();
	      followupDiv.hide();
	    } else {
	      container.show();
	    }
	  });

	  // When any applicantRaceAndEthnicity checkbox is selected, uncheck "Prefer not to say"
	  var applicantRaceInputs = document.querySelectorAll('input[name="applicantRaceAndEthnicity[]"]');
	  applicantRaceInputs.forEach(function (raceInput) {
	    raceInput.addEventListener('change', function () {
	      if (this.checked || (this.closest('label') && this.closest('label').classList.contains('is-selected'))) {
	        preferNotToSayCheckbox.checked = false;
	        if (preferNotToSayCheckbox.closest('label')) preferNotToSayCheckbox.closest('label').classList.remove('is-selected');
	      }
	      // When "Some other race or ethnicity" is selected, uncheck all other race checkboxes
	      if (this.value === 'SOME_OTHER_RACE_OR_ETHNICITY' && (this.checked || (this.closest('label') && this.closest('label').classList.contains('is-selected')))) {
	        applicantRaceInputs.forEach(function (other) {
	          if (other !== raceInput) {
	            other.removeAttribute('checked');
	            other.checked = false;
	            if (other.closest('label')) other.closest('label').classList.remove('is-selected');
	          }
	        });
	      }
	    });
	  });
	});
  </script>
</div>
