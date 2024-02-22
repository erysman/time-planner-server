package com.pw.timeplanner.core.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.zalando.problem.spring.web.advice.ProblemHandling;
import org.zalando.problem.spring.web.advice.security.SecurityAdviceTrait;

@ControllerAdvice
public class ControllerAdviceHandler implements ProblemHandling, SecurityAdviceTrait {

}
