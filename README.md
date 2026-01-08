private List<String> extractHouseholdMemberNames(List<String> values) {
    List<String> memberNames = new ArrayList<>();
    for (String value : values) {
      if (isHouseholdMemberValue(value)) {
        String formattedName = format(value);
        if (!formattedName.isBlank()) {
          memberNames.add(formattedName);
        }
      }
    }
    return memberNames;
  }

  private boolean isHouseholdMemberValue(String value) {
    return !value.equals("true") && !value.equals("false");
  }
