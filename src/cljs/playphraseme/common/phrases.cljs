(ns playphraseme.common.phrases
  (:require [clojure.string :as string]
            [playphraseme.common.util :as util]))

(def all-phrases-pos (atom 0))

(def all-phrases
  (->>
   ["l thought" "told you about" "I would have" "i'd have" "figured out" "what is wrong with you"
    "take care" "if you know what i mean" "right now" "over there" "will give you" "I thought" "" "don't you think"
    "each other" "not at all" "anything at all" "each other" "could do that" "I knew that" "I've got to"
    "nothing to do" "What's happening" "appreciate it" "don't even have" "so be it" "stick around" "somehow"
    "good thing" "I'll tell you" "look at you" "too late" "let me know" "for a while" "where is my" "that's what she said"
    "can I talk to you" "I'm not sure" "look out" "I'm guessing" "haven't seen" "so to speak" "At your service"
    "may i" "I'll pay" "i owe you" "I'll take it" "I'm hungry" "give up" "out of the blue" "give me a break"
    "A little bit" "Be careful" "See you soon" "look forward" "shame on" "Are you kidding me" "it's your turn"
    "I'd like to" "Don't worry about" "A long time ago" "Good luck" "i'm looking forward" "High five"
    "Leave me alone" "You're welcome" "Merry Christmas" "That's all right" "Let's do this" "Why did you do that"
    "what's wrong with you" "have fun" "Happy birthday" "I don't understand" "cannot do this" "you need something"
    "whether you like it or not" "Wait up" "chill out" "are you talking to" "I wouldn't do that" "will you please"
    "would you like" "oh, my god" "count on" "You're the best" "i've never been to" "as you wish" "stay with me"
    "pretty cool" "How was your day" "Have you ever been to" "It's about time" "say hello to" "for the time being"
    "May I ask you" "Let me get this straight" "back and forth" "I beg your pardon" "This is ridiculous"
    "How long has it been" "let me down" "come on let's go"]
   (sort-by #(rand-int 1000))
   vec))

(defn inc-value [v max]
  (let [new-v (inc v)]
    (if (>= new-v max)
      0 new-v)))

(def all-bad-phrases-pos (atom 0))

(defn random-phrase []
  (reset! all-phrases-pos (inc-value @all-phrases-pos (count all-phrases)))
  (nth all-phrases @all-phrases-pos))

(def all-bad-phrases
  (->>
   ["fuck you" "bitter end" "wanker" "fuck off" "fuck up" "bitch" "pussy" "holy shit"
    "holy crap" "crap out" "ass hat" "asshole" "fucker" "blowjob" "dumbass" "dickhead" "no fucking way"
    "motherfucker" "What the fuck"  "oh  fuck"]
   (sort-by rand-int)
   vec))

(defn random-bad-phrase []
  (reset! all-bad-phrases-pos (inc-value @all-bad-phrases-pos (count all-bad-phrases)))
  (nth all-bad-phrases @all-bad-phrases-pos))

(defn search-random-phrase []
  (util/go-url! (str "#/search?q=" (random-phrase))))

(defn search-random-bad-phrase []
  (util/go-url! (str "#/search?q=" (random-bad-phrase))))

(defn phrase? [obj]
  (some-> obj (contains? :words)))

(comment
  (search-random-phrase)


  )
