(ns pour.core-test
  (:use [pour.core] :reload-all)
  (:use [pour.validators
         :only [required email-address minimum-length a-number optional]]
        :reload-all)
  (:use [clojure.test]))


(defform test-form-1
  :login [required "login required"
          email-address "login email address"]
  :password [required "password required"
             (minimum-length 5) "password length >= 5"])

(deftest valid-form-input
  (let [login "foo@bar.com"
        password "12345"]
    (is (= {:errors {} :values {:login login :password password}}
           (test-form-1 {:login login :password password})))
    (is (= {:login login :password password}
           (values (test-form-1 {:login login :password password}))))))

(deftest invalid-form-input
  (let [empty-login ""
        invalid-login "foo"
        password "12345"]
    (is (= {:errors {:login "login required"}
            :values {:login nil :password password}}
           (test-form-1 {:login empty-login :password password})))
    (is (= {:errors {:login "login email address"}
            :values {:login nil :password password}}
           (test-form-1 {:login invalid-login :password password})))
    (is (errors? (test-form-1 {})))
    (is (= {:login "login required"}
           (errors (test-form-1 {:login nil :password password}))))))

(deftest missing-form-fields-are-checked
  (let [login "foo@bar.com"
        password "12345"]
    (is (= {:errors {:login "login required"}
            :values {:login nil :password password}}
           (test-form-1 {:password password})))
    (is (= {:errors {:password "password required"}
            :values {:login login :password nil}}
           (test-form-1 {:login login})))
    (is (= {:errors {:login "login required"
                     :password "password required"}
            :values {:login nil :password nil}}
           (test-form-1 {})))))

(defform test-form-2
  :age [required "age required"
        a-number "age number"])

(deftest value-conversion-in-forms
  ;; String "14" is converted to Integer 14
  (is (= {:errors {} :values {:age 14}}
         (test-form-2 {:age "14"})))
  ;; required throws an exception on Integers
  (is (thrown? RuntimeException
               (test-form-2 {:age 14}))))

(defform test-form-3
  :opt [(optional 0) ""
        #(* % 10) ""])

(deftest short-circuiting-in-forms
  (is (= {:errors {} :values {:opt 0}}
         (test-form-3 {})))
  (is (= {:errors {} :values {:opt 0}}
         (test-form-3 {:opt nil})))
  (is (= {:errors {} :values {:opt 10}}
         (test-form-3 {:opt 1}))))

(deftest short-circuit-fns
  (let [v "value"
        s (stop-with-value v)]
    (is (stop? (first s)))
    (is (= v (last s)))))
