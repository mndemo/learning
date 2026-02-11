package org.codeforamerica.shiba.pages.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Value;
import org.codeforamerica.shiba.inputconditions.Condition;
import org.codeforamerica.shiba.pages.config.Validation;
import org.codeforamerica.shiba.pages.config.Validator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value
public class InputData implements Serializable {

  @Serial
  private static final long serialVersionUID = 8511070147741948268L;

  @NotNull List<String> value;
  @NotNull
  @JsonIgnore
  List<Validator> validators;

  InputData(List<String> value, @NotNull List<Validator> validators) {
    this.value = Objects.requireNonNullElseGet(value, List::of);
    this.validators = Objects.requireNonNullElseGet(validators, List::of);
  }

  InputData() {
    this(new ArrayList<>(), new ArrayList<>());
  }

  public InputData(@NotNull List<String> value) {
    this(value, new ArrayList<>());
  }

  public Boolean valid(PageData pageData) {
    return valid(pageData, null);
  }

  public Boolean valid(PageData pageData, @Nullable ApplicationData applicationData) {
    return validators.stream().filter(
            validator -> validatorShouldRun(validator, pageData, applicationData))
        .allMatch(validator -> {
          Validation validation = validator.getValidation();
          if (validator.getStartDateInputName() != null) {
            return Validation.applyWithPageData(validation, value, pageData, validator);
          }
          return validation.apply(value);
        });
  }

  private boolean validatorShouldRun(Validator validator, PageData pageData, @Nullable ApplicationData applicationData) {
    if (validator.getCondition() == null) return true;
    Condition condition = validator.getCondition();
    if (condition.getCustomCondition() != null && applicationData != null) {
      return condition.satisfies(pageData, applicationData);
    }
    return condition.satisfies(pageData);
  }
  
  // This method is only called from schoolStartDateInput.html
  public Boolean valid(String input) {
	  List<String> inputList = Arrays.asList(input.split(",", -1));
	  boolean isEmpty = inputList.stream().allMatch(string -> string.isEmpty());
	  if(isEmpty) {
		  return true;
	  }
	  
	  boolean isValid = validators.stream().filter(
	            validator -> validator.getCondition() == null || validator.getCondition()
                .satisfies(input)).map(Validator::getValidation)
        .allMatch(validation -> validation.apply(inputList));
	  return isValid;
	  }

  public List<String> errorMessageKeys(PageData pageData) {
    return errorMessageKeys(pageData, null);
  }

  public List<String> errorMessageKeys(PageData pageData, @Nullable ApplicationData applicationData) {
    return errorMessageKeysImpl(value, pageData, applicationData);
  }

  /**
   * Returns whether the value at the given person index passes all validators.
   * Used for household follow-up inputs so errors can be shown next to the correct person.
   * @param stride 1 for single-value inputs (MONEY, SELECT, CHECKBOX), 3 for DATE
   */
  public boolean validForIndex(PageData pageData, @Nullable ApplicationData applicationData,
      int personIndex, int stride) {
    List<String> slice = valueSliceForIndex(personIndex, stride);
    return validators.stream()
        .filter(validator -> validatorShouldRun(validator, pageData, applicationData))
        .allMatch(validator -> {
          Validation validation = validator.getValidation();
          if (validator.getStartDateInputName() != null && pageData != null) {
            List<String> startSlice = getStartDateSlice(pageData, validator.getStartDateInputName(), personIndex);
            return Validation.applyWithPageDataForSlice(validation, slice, startSlice, validator);
          }
          return validation.apply(slice);
        });
  }

  /**
   * Returns error message keys for the value at the given person index only.
   */
  public List<String> errorMessageKeysForIndex(PageData pageData, @Nullable ApplicationData applicationData,
      int personIndex, int stride) {
    List<String> slice = valueSliceForIndex(personIndex, stride);
    return errorMessageKeysImplWithSlice(slice, pageData, applicationData, personIndex, stride);
  }

  private List<String> valueSliceForIndex(int personIndex, int stride) {
    int from = personIndex * stride;
    int to = Math.min(value.size(), from + stride);
    if (from >= value.size()) {
      return stride == 3 ? List.of("", "", "") : List.of("");
    }
    if (stride == 3) {
      if (to - from == 3) return value.subList(from, to);
      List<String> out = new ArrayList<>();
      for (int i = 0; i < 3; i++) out.add(from + i < value.size() ? value.get(from + i) : "");
      return out;
    }
    return List.of(from < value.size() ? value.get(from) : "");
  }

  private List<String> getStartDateSlice(PageData pageData, String startDateInputName, int personIndex) {
    InputData startData = pageData.get(startDateInputName);
    if (startData == null || startData.getValue() == null) return List.of("", "", "");
    List<String> v = startData.getValue();
    int from = personIndex * 3;
    if (from + 3 > v.size()) return List.of("", "", "");
    return new ArrayList<>(v.subList(from, from + 3));
  }

  private List<String> errorMessageKeysImpl(List<String> valuesToValidate, PageData pageData,
      @Nullable ApplicationData applicationData) {
    return validators.stream()
        .filter(validator ->
            (validator.getCondition() == null
                || (applicationData != null
                    ? validator.getCondition().satisfies(pageData, applicationData)
                    : validator.getCondition().satisfies(pageData)))
            && !(validator.getStartDateInputName() != null
                ? Validation.applyWithPageData(validator.getValidation(), valuesToValidate, pageData, validator)
                : validator.getValidation().apply(valuesToValidate)))
        .map(Validator::getErrorMessageKey).collect(Collectors.toList());
  }

  private List<String> errorMessageKeysImplWithSlice(List<String> slice, PageData pageData,
      @Nullable ApplicationData applicationData, int personIndex, int stride) {
    return validators.stream()
        .filter(validator ->
            (validator.getCondition() == null
                || (applicationData != null
                    ? validator.getCondition().satisfies(pageData, applicationData)
                    : validator.getCondition().satisfies(pageData)))
        .filter(validator -> {
          if (validator.getStartDateInputName() != null && pageData != null) {
            List<String> startSlice = getStartDateSlice(pageData, validator.getStartDateInputName(), personIndex);
            return !Validation.applyWithPageDataForSlice(validator.getValidation(), slice, startSlice, validator);
          }
          return !validator.getValidation().apply(slice);
        })
        .map(Validator::getErrorMessageKey).collect(Collectors.toList());
  }

  public String getValue(int i) {
    return this.getValue().get(i);
  }

  public void setValue(String newValue, int i) {
    this.value.set(i, newValue);
  }

	@Override
	public String toString() {
		return "InputData [value=" + value + ", validators=" + validators + "]";
	}
}
