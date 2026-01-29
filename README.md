 - name: socialSecurityFrequency
        type: SELECT
        options:
          selectableOptions:
            - value: MONTHLY
              messageKey: unearned-income.frequency.monthly
            - value: TWICE_A_MONTH
              messageKey: unearned-income.frequency.twice-a-month
            - value: WEEKLY
              messageKey: unearned-income.frequency.weekly
            - value: OTHER
              messageKey: unearned-income.frequency.other
        promptMessage:
          promptMessageKey: unearned-income-source.frequency-of-income
        condition:
          pageName: unearnedIncome
          input: unearnedIncome
          value: SOCIAL_SECURITY
      - name: socialSecurityStartDate
        type: DATE
        promptMessage:
          promptMessageKey: unearned-income-source.start-date
        condition:
          pageName: unearnedIncome
          input: unearnedIncome
          value: SOCIAL_SECURITY
      - name: socialSecurityEndDate
        type: DATE
        promptMessage:
          promptMessageKey: unearned-income-source.end-date
        helpMessageKey: unearned-income-source.no-end-date-help
        condition:
          pageName: unearnedIncome
          input: unearnedIncome
          value: SOCIAL_SECURITY
      - name: socialSecurityNoEndDate
        type: CHECKBOX
        promptMessage:
          promptMessageKey: unearned-income-source.no-end-date
        options:
          selectableOptions:
            - value: "true"
              messageKey: unearned-income-source.no-end-date
        condition:
          pageName: unearnedIncome
          input: unearnedIncome
          value: SOCIAL_SECURITY
