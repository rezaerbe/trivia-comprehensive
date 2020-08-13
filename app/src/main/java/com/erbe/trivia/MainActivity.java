package com.erbe.trivia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Color;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.erbe.trivia.data.AnswerListAsyncResponse;
import com.erbe.trivia.data.QuestionBank;
import com.erbe.trivia.model.Question;
import com.erbe.trivia.model.Score;
import com.erbe.trivia.util.Prefs;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView questionTextView, questionCounterTextView, highestScoreTextView, scoreTextView;
    private Button trueButton, falseButton, shareButton;
    private ImageButton nextButton, prevButton;

    private int currentQuestionIndex = 0;

    private List<Question> questionList;

    private int scoreCounter = 0;
    private Score score;
    private Prefs prefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        score = new Score();

        prefs = new Prefs(MainActivity.this);

        nextButton = findViewById(R.id.next_button);
        prevButton = findViewById(R.id.prev_button);
        trueButton = findViewById(R.id.true_button);
        falseButton = findViewById(R.id.false_button);
        questionCounterTextView = findViewById(R.id.counter_text);
        questionTextView = findViewById(R.id.question_textview);
        shareButton = findViewById(R.id.shareButton);
        scoreTextView = findViewById(R.id.score_text);
        highestScoreTextView = findViewById(R.id.highest_score);

        nextButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);
        trueButton.setOnClickListener(this);
        falseButton.setOnClickListener(this);
        shareButton.setOnClickListener(this);

        scoreTextView.setText(MessageFormat.format("Current Score: {0}", String.valueOf(score.getScore())));

        currentQuestionIndex = prefs.getState();

        highestScoreTextView.setText(MessageFormat.format("Highest Score: {0}", String.valueOf(prefs.getHighScore())));

        questionList = new QuestionBank().getQuestions(new AnswerListAsyncResponse() {
            @Override
            public void processFinished(ArrayList<Question> questionArrayList) {

                questionTextView.setText(questionArrayList.get(currentQuestionIndex).getAnswer());
                String cqi = currentQuestionIndex + " / " + questionArrayList.size();
                questionCounterTextView.setText(cqi);
            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.prev_button:
                if (currentQuestionIndex > 0) {
                    currentQuestionIndex = (currentQuestionIndex - 1) % questionList.size();
                    updateQuestion();
                }
                break;
            case R.id.next_button:
                goNext();
                break;
            case R.id.true_button:
                checkAnswer(true);
                updateQuestion();
                break;
            case R.id.false_button:
                checkAnswer(false);
                updateQuestion();
                break;
            case R.id.shareButton:
                shareScore();
                break;
        }
    }


    public void shareScore() {
        String message = "My current score is " + score.getScore() + " and "
                + "My highest score is " + prefs.getHighScore();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "I am Playing Trivia");
        intent.putExtra(Intent.EXTRA_TEXT, message);
        startActivity(intent);
    }


    private void checkAnswer(boolean userChooseCorrect) {

        boolean answerIsTrue = questionList.get(currentQuestionIndex).isAnswerTrue();
        int toastMessageId = 0;
        if (userChooseCorrect == answerIsTrue) {
            fadeView();
            addPoints();
            toastMessageId = R.string.correct_answer;
        } else {
            shakeAnimation();
            deductPoints();
            toastMessageId = R.string.wrong_answer;
        }
        Toast.makeText(this, toastMessageId, Toast.LENGTH_SHORT).show();
    }


    private void addPoints() {
        scoreCounter += 100;
        score.setScore(scoreCounter);
        scoreTextView.setText(MessageFormat.format("Current Score: {0}", String.valueOf(score.getScore())));
    }


    private void deductPoints() {
        scoreCounter -= 100;
        if (scoreCounter > 0) {
            score.setScore(scoreCounter);
            scoreTextView.setText(MessageFormat.format("Current Score: {0}", String.valueOf(score.getScore())));
        } else {
            scoreCounter = 0;
            score.setScore(scoreCounter);
            scoreTextView.setText(MessageFormat.format("Current Score: {0}", String.valueOf(score.getScore())));
        }
    }


    private void updateQuestion() {

        String question = questionList.get(currentQuestionIndex).getAnswer();
        questionTextView.setText(question);
        questionCounterTextView.setText(MessageFormat.format("{0} / {1}", currentQuestionIndex, questionList.size()));
    }


    private void fadeView() {

        final CardView cardView = findViewById(R.id.cardView);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);

        alphaAnimation.setDuration(500);
        alphaAnimation.setRepeatCount(1);
        alphaAnimation.setRepeatMode(Animation.REVERSE);

        cardView.setAnimation(alphaAnimation);

        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                cardView.setCardBackgroundColor(Color.GREEN);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardView.setCardBackgroundColor(Color.WHITE);
                goNext();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    private void shakeAnimation() {

        final CardView cardView = findViewById(R.id.cardView);
        Animation shake = AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake_animation);

        cardView.setAnimation(shake);

        shake.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                cardView.setCardBackgroundColor(Color.RED);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardView.setCardBackgroundColor(Color.WHITE);
                goNext();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    private void goNext() {
        currentQuestionIndex = (currentQuestionIndex + 1) % questionList.size();
        updateQuestion();
    }


    @Override
    protected void onPause() {
        prefs.saveHighScore(score.getScore());
        prefs.setState(currentQuestionIndex);
        super.onPause();
    }
}