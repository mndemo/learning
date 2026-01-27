    followUpValues:
          - "true"
        followUps:
          - name: disqualifiedPublicAssistance-householdMembers
            type: CUSTOM
            customInputFragment: penaltyWarningHouseholdFollowup
            validators:
              - validation: SELECT_AT_LEAST_ONE
                errorMessageKey: penalty-warning.validation.select-household-member
