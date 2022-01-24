package com.kyu.calculator;

/*
    1. 빈 문자열 또는 null 값을 입력할 경우 0을 반환해야 한다.
       "" => 0
       null => 0

    2. 숫자 하나를 문자열로 입력할 경우 해당 숫자를 반환한다.
        "1" => 1

    3. 숫자 두개를 쉼표(.) 구분자로 입력할 경우 두 숫자의 합을 반환한다.
        "1,2" => 3

    4. 구분자를 쉼표(,) 이외에 콜론(;)을 사용할 수 있다.
        "1,2:3" => 6

    5. "//"와 "\n" 문자 사이에 커스텀 구분자를 지정할 수 있다.
        "//;\n1;2;3" => 6

    6. 계산기에 음수를 전달하는 경우 RuntimeException 예외를 throw한다.

 */

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringCalculator {

    public int add(String text) {
        if(isBlank(text)) {
            return 0;
        }

        return sum(toInt(textSplit(text)));
    }

    private boolean isBlank(String text) {
        return text == null || text.isEmpty();
    }

    private String[] textSplit(String text) {
        Matcher m = Pattern.compile("//(.)\n(.*)").matcher(text);

        if(m.find()) {
            String customDelimeter = m.group(1);
            return m.group(2).split(customDelimeter);
        }

        return text.split(",|:");
    }

    private int[] toInt(String[] values) {
        int[] numbers = new int[values.length];
        for(int i = 0; i < values.length  ; i++) {
            numbers[i] = toPositive(values[i]);
        }

        return numbers;
    }

    private int toPositive(String value) {
        int number = Integer.parseInt(value);

        if(number < 0) {
            throw new RuntimeException("음수 값이 존재합니다.");
        }

        return number;
    }

    private int sum(int[] numbers) {
        int sum = 0;

        for(int num : numbers) {
            sum += num;
        }

        return sum;
    }

}
