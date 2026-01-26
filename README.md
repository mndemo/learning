SELECT_AT_LEAST_ONE_IF_YES_SELECTED(strings -> {
    if (strings.contains("true")) {
      // Check if any value looks like a household member name
      // Household member values contain "applicant" or have multiple words (name + iteration id)
      boolean hasHouseholdMemberPattern = strings.stream()
          .anyMatch(s -> s.contains("applicant") || (s.contains(" ") && s.split(" ").length >= 2 && !s.equals("true") && !s.equals("false")));
      
      // If we detect household member patterns in the list, require at least one to be selected
      // If no patterns detected, assume user lives alone (fragment doesn't show checkboxes) and allow just "true"
      if (hasHouseholdMemberPattern) {
        return strings.size() > 1;
      }
      // No household member patterns - likely user lives alone, allow just "true"
      return true;
    }
