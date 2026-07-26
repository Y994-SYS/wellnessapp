package com.alkanyazilim.wellnesapp.ui.workout

import androidx.annotation.DrawableRes
import com.alkanyazilim.wellnesapp.R

fun drawableForPose(pose: ExercisePose): Int? = when (pose) {
    ExercisePose.STANDING -> null
    ExercisePose.JUMPING_JACK -> R.drawable.pose_jumping_jack
    ExercisePose.HIGH_KNEE -> R.drawable.pose_high_knee
    ExercisePose.JUMP_ROPE -> R.drawable.pose_jump_rope
    ExercisePose.PLANK -> R.drawable.pose_plank
    ExercisePose.PUSHUP -> R.drawable.pose_pushup
    ExercisePose.SITUP -> R.drawable.pose_situp
    ExercisePose.SQUAT -> R.drawable.pose_squat
    ExercisePose.LUNGE -> R.drawable.pose_lunge
    ExercisePose.PULLUP -> R.drawable.pose_pullup
    ExercisePose.NECK_STRETCH -> R.drawable.pose_neck_stretch
    ExercisePose.ARM_STRETCH -> R.drawable.pose_arm_stretch
    ExercisePose.BUTTERFLY -> R.drawable.pose_butterfly
    ExercisePose.HAMSTRING_STRETCH -> R.drawable.pose_hamstring_stretch
    ExercisePose.CAT_COW -> R.drawable.pose_cat_cow
    ExercisePose.CHILD_POSE -> R.drawable.pose_child_pose
}