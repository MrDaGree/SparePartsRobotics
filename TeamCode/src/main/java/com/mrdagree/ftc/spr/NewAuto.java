package com.mrdagree.ftc.spr;

import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;
import com.vuforia.CameraDevice;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import java.util.List;


@Autonomous(name = "Autonomous", group = "")
//@Disabled
public class NewAuto extends LinearOpMode
{

    public enum direction {
      LEFT,
      RIGHT,
      FORWARD,
      BACKWARD,
      ROTATE_LEFT,
      ROTATE_RIGHT
    };

    public enum goldLocation {
        MIDDLE,
        RIGHT,
        LEFT,
        UNKNOWN
    };

    goldLocation location = goldLocation.UNKNOWN;
    boolean craterSide = true;

    Robot robot;

    // State used for updating telemetry
    Orientation angles;
    Acceleration gravity;

    static final double     COUNTS_PER_MOTOR_REV    = 280.0 ;    // Started at 28.0 -- eg: AndyMark NeverRest40 Motor Encoder
    static final double     DRIVE_GEAR_REDUCTION    = 40.0 ;     // Started at 40.0 -- This is < 1.0 if geared UP
    static final double     WHEEL_DIAMETER_INCHES   = 4.0 ;     // For figuring circumference
    static final double     COUNTS_PER_INCH         = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
            (WHEEL_DIAMETER_INCHES * 3.1415);
    static final double     DRIVE_SPEED             = 0.95;
    static final double     TURN_SPEED              = .35;

    // Setup variables
    private ElapsedTime runtime = new ElapsedTime();
    private static final float mmPerInch        = 25.4f;
    private static final float mmFTCFieldWidth  = (12*6) * mmPerInch;       // the width of the FTC field (from the center point to the outer panels)
    private static final float mmTargetHeight   = (6) * mmPerInch;          // the height of the center of the target image above the floor

    // TenserFlow Variables
    private static final String VUFORIA_KEY = "AdSjclj/////AAABGbudF+8chkyGrnyeBGgBf3RWw3N5uISbrxyexQQo09reYhiaN4jWt26ZHHrMXtqJS07ib7fbGBlJI1D69MlboR1gaPg/7MK0/BQ8HwmBrCHvU+yo60VotB/Y4Y5uLbC2eag+w5+AMku+cSKfVNxP52UbVdS4IjzDb6wuWamTGWuY1FVldHyVpU5gUQJZc71heZzI/KKwGr1xbnB12V4hb/xwxpr8kWMVSBknAPpk65keCGOaNsISrMoi/Gl6xvKC0Q2MNEtkX0XUG6ZhhKII0JrpwPsZX8G79Pvi/t3aPb9WQ0ddcuX+USXOO9q4snR+9KwBziiT3wkBQ0qdPLV3ErTeXUxeSfB1qFtfDA3p2UbW";

    private static final String TFOD_MODEL_ASSET = "RoverRuckus.tflite";
    private static final String LABEL_GOLD_MINERAL = "Gold Mineral";
    private static final String LABEL_SILVER_MINERAL = "Silver Mineral";
    private VuforiaLocalizer vuforia;
    private TFObjectDetector tfod;


    public int step = 0;
    public int middleCheck = 0;
    public int rightCheck = 0;


    @Override
    public void runOpMode() {
        //Init everything
        robot = new Robot(hardwareMap);
        initVuforia();
        if (ClassFactory.getInstance().canCreateTFObjectDetector()) {
            initTfod();
        } else {
            telemetry.addData("Sorry!", "This device is not compatible with TFOD");
        }

        CameraDevice.getInstance().setFlashTorchMode(true);

        while (!opModeIsActive()){
            if (gamepad1.a)
                craterSide = !craterSide;

            if (robot.bottomLift.getState()) {
                robot.hangingMotor.setPower(-1.0);
                robot.theGoodStuff.setPattern(RevBlinkinLedDriver.BlinkinPattern.SINELON_FOREST_PALETTE);
            }else{ robot.hangingMotor.setPower(0.0); }

            robot.hangingMotor.setPower(gamepad1.right_trigger);

            robot.theGoodStuff.setPattern(RevBlinkinLedDriver.BlinkinPattern.RAINBOW_WITH_GLITTER);

            telemetry.addData("Crater Side (GP 1 A)", craterSide ? "yes" : "no");
            telemetry.update();
        }

        if (opModeIsActive()) {
            /** Activate Tensor Flow Object Detection. */
            if (tfod != null) {
                tfod.activate();
            }
            runtime.reset();
        }

        while (opModeIsActive()){
            switch (step){
                //UNLATCHING
                case 0:
                    // Should get hanging going
                    if (!robot.bottomLift.getState() && robot.topLift.getState()) {
                        robot.hangingMotor.setPower(1.0);
                        robot.theGoodStuff.setPattern(RevBlinkinLedDriver.BlinkinPattern.SINELON_FOREST_PALETTE);
                    }else if (robot.bottomLift.getState() && robot.topLift.getState()){
                        // Senses that both the magnetic limit switches arent triggered so keeps power to decend.
                        robot.hangingMotor.setPower(1.0);
                        robot.theGoodStuff.setPattern(RevBlinkinLedDriver.BlinkinPattern.SINELON_FOREST_PALETTE);
                    }
                    else if (!robot.topLift.getState() && robot.bottomLift.getState()) {
                        // Senses that the top is triggered and bottom is not thus the lift should be at the top.
                        robot.hangingMotor.setPower(-0.25);
                        sleep(300);
                        robot.hangingMotor.setPower(0);
                        sleep(300);
                        move(0.4,0.5,1.0, direction.LEFT);
                        robot.theGoodStuff.setPattern(RevBlinkinLedDriver.BlinkinPattern.HEARTBEAT_RED);
                        sleep(200);
                        robot.theGoodStuff.setPattern(RevBlinkinLedDriver.BlinkinPattern.BLACK);
                        step = 1;
                    }
                    break;
                //MOVE FORWARD
                case 1:
                    robot.theGoodStuff.setPattern(RevBlinkinLedDriver.BlinkinPattern.SINELON_RAINBOW_PALETTE);
                    move(DRIVE_SPEED, 1.3, 1.0, direction.FORWARD);
                    sleep(500);
                    step = 2;
                    break;
//                //MIDDLE CHECK, CUZ WHY NOT WE HERE ANYWAYS
                case 2:
                    robot.theGoodStuff.setPattern(RevBlinkinLedDriver.BlinkinPattern.BLACK);
                    sleep(2000);
                    while (middleCheck <= 200)
                    {
                        telemetry.addData("Middle Check", middleCheck);
                        telemetry.update();
                        if (checkForGold()){
                            location = goldLocation.MIDDLE;
                            step = 6;
                            break;
                        }else{ middleCheck += 1; }
                    }
                    if (step == 2)
                        step = 3;

                    break;
//                //MOVE TO THE RIGHT SPOT, ONLY CUZ IT WASNT IN THE MIDDLE ALREADY
                case 3:
                    robot.theGoodStuff.setPattern(RevBlinkinLedDriver.BlinkinPattern.HEARTBEAT_RED);
                    gyroTurn(0.6, -35.0, 0);
                    step = 4;
                    break;
//                //RIGHT SIDE CHECK
                case 4:
                    robot.theGoodStuff.setPattern(RevBlinkinLedDriver.BlinkinPattern.BLACK);
                    sleep(2000);
                    while (rightCheck <= 200)
                    {
                        telemetry.addData("Right Check", rightCheck);
                        telemetry.update();
                        if (checkForGold()){
                            gyroTurn(0.6, -55.0, 0);
                            sleep(300);
                            location = goldLocation.RIGHT;
                            step = 6;
                            break;
                        }else{ rightCheck += 1; }
                    }
                    if (step == 4)
                        step = 5;

                    break;
                case 5:
                    robot.theGoodStuff.setPattern(RevBlinkinLedDriver.BlinkinPattern.HEARTBEAT_RED);
                    gyroTurn(0.6, 35.0, 0);
                    location = goldLocation.LEFT;
                    step = 6;
                    break;
                // IF IT FINDS THE CUBE IT RUNS THIS AND THEN GOES TO STEP 21
                // ALSO IF IT CANNOT FIND IT IN ANY OF THE OTHER SPOTS IT RUNS THIS ON LEFT SIDE
                case 6:
                    robot.theGoodStuff.setPattern(RevBlinkinLedDriver.BlinkinPattern.HEARTBEAT_RED);
                    if (location == goldLocation.RIGHT){
                        move(DRIVE_SPEED, 1.8, 1.0, direction.FORWARD);
                        step = 9;
                    }else if (location == goldLocation.MIDDLE){
                        move(DRIVE_SPEED, 1.3, 1.0, direction.FORWARD);
                        step = 9;
                    }else if (location == goldLocation.LEFT){

                        step = 9;
                    }
                    step = 9;
                    break;
                // MOVE TO LOCATION TO START MOVE TO DEPO
                case 9:
                    if (location == goldLocation.RIGHT){
                        move(DRIVE_SPEED, 2.18, 1.0, direction.BACKWARD);
                        gyroTurn(0.6, 65.0, 0);
                        step = 10;
                    }else if (location == goldLocation.MIDDLE){
                        move(DRIVE_SPEED, 1.3, 1.0, direction.BACKWARD);
                        gyroTurn(0.6, 65.0, 0);
                        step = 10;
                    }else if (location == goldLocation.LEFT){

                    }
                    break;
                case 10:
                    sleep(300);
                    move(DRIVE_SPEED, 3.3, 1.0, direction.FORWARD);
                    step = 11;
                    break;

            }
            telemetry.addData("Step", step);
            telemetry.update();
        }

        CameraDevice.getInstance().setFlashTorchMode(false);

        if (tfod != null) {
            tfod.shutdown();
        }
    }

    public boolean checkForGold(){
        boolean found = false;
        if (tfod != null) {
            List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();

            if (updatedRecognitions != null) {
                telemetry.addData("# Object Detected", updatedRecognitions.size());
                if (updatedRecognitions.size() == 1) {
                    for (Recognition recognition : updatedRecognitions) {
                        if (recognition.getLabel().equals(LABEL_GOLD_MINERAL)) {
                            found = true;
                        }
                    }
                }
            }
        }

        return found;
    }


    private void initVuforia() {
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         */
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Loading trackables is not necessary for the Tensor Flow Object Detection engine.
    }

    private void initTfod() {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_GOLD_MINERAL, LABEL_SILVER_MINERAL);
    }

    public void move(double speed, double inch, double timeout, direction direct){
        switch (direct){
            case BACKWARD:
                forward(speed, inch, timeout);
                break;
            case FORWARD:
                backward(speed, inch, timeout);
                break;
            case RIGHT:
                left(speed, inch, timeout);
                break;
            case LEFT:
                right(speed, inch, timeout);
                break;
            default:
                forward(speed, inch, timeout);
                break;
        }
    }

    public void gyroTurn (  double speed, double angle, int side) {

        // keep looping while we are still active, and not on heading.
        while (opModeIsActive() && !rotateToDegree(speed, angle, 2.0, side)) {
            // Update telemetry & Allow time for other processes to run.
            telemetry.update();
        }
    }


    public boolean rotateToDegree(double speed, double degree, double tolerance, int side){
        angles = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
//        telemetry.addData("Current Angle", angles.firstAngle);
//        telemetry.addData("Angle", degree);
//        telemetry.addData("Tolerance", "%5.2f | %5.2f", degree + tolerance, degree - tolerance);
//        telemetry.update();

        // 15 degrees (14.2) (15.8)

        boolean goalReached = false;

        if ((angles.firstAngle <= degree + tolerance) && (angles.firstAngle >= degree - tolerance)) {
            goalReached = true;
        }
        if (goalReached) {
            robot.frontLeft.setPower(0);
            robot.frontRight.setPower(0);
            robot.rearLeft.setPower(0);
            robot.rearRight.setPower(0);
        }
        if (!goalReached) {
            if (degree < angles.firstAngle) {
                if (side == -1 || side == 0) {
                    robot.frontLeft.setPower(-0.7);
                    robot.rearLeft.setPower(-0.7);
                }

                if (side == 1 || side == 0) {
                    robot.frontRight.setPower(0.7);
                    robot.rearRight.setPower(0.7);
                }
            }else{
                if (side == -1 || side == 0) {
                    robot.frontLeft.setPower(0.7);
                    robot.rearLeft.setPower(0.7);
                }

                if (side == 1 || side == 0) {
                    robot.frontRight.setPower(-0.7);
                    robot.rearRight.setPower(-0.7);
                }
            }
        }

        return goalReached;
    }

    public void forward(double speed, double inch, double timeout){
        encoderDrive(speed, inch, inch, inch, inch, timeout);
    }

    public void backward(double speed, double inch, double timeout){
        encoderDrive(speed, -inch, -inch, -inch, -inch, timeout);
    }

    public void left(double speed, double inch, double timeout){
        encoderDrive(speed, inch, -inch, -inch, inch, timeout);
    }

    public void right(double speed, double inch, double timeout){
        encoderDrive(speed, -inch, inch, inch, -inch, timeout);
    }

    public void encoderDrive(double speed,
                             double frontLeftWheel, double rearLeftWheel, double frontRightWheel,double rearRightWheel,
                             double timeoutS) {
        int new_tLeftTarget;
        int new_tRightTarget;
        int new_bLeftTarget;
        int new_bRightTarget;

        Orientation firstAngles = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        Orientation drivingAngles;

        // Ensure that the opmode is still active
        if (opModeIsActive()) {

            // Determine new target position, and pass to motor controller
            new_tLeftTarget = robot.frontLeft.getCurrentPosition() + (int) (frontLeftWheel * COUNTS_PER_INCH);
            new_tRightTarget = robot.frontRight.getCurrentPosition() + (int) (frontRightWheel * COUNTS_PER_INCH);
            new_bLeftTarget = robot.rearLeft.getCurrentPosition() + (int) (rearLeftWheel * COUNTS_PER_INCH);
            new_bRightTarget = robot.rearRight.getCurrentPosition() + (int) (rearRightWheel * COUNTS_PER_INCH);
            robot.frontLeft.setTargetPosition(new_tLeftTarget);
            robot.frontRight.setTargetPosition(new_tRightTarget);
            robot.rearLeft.setTargetPosition(new_bLeftTarget);
            robot.rearRight.setTargetPosition(new_bRightTarget);

            // Turn On RUN_TO_POSITION
            robot.frontLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.frontRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.rearLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.rearRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            // reset the timeout time and start motion.
            runtime.reset();
            robot.frontLeft.setPower(speed);
            robot.frontRight.setPower(speed * 2);
            robot.rearLeft.setPower(speed);
            robot.rearRight.setPower(speed * 2);

            // keep looping while we are still active, and there is time left, and both motors are running.
            while (opModeIsActive() &&
                    (runtime.seconds() < timeoutS) &&
                    (robot.frontLeft.isBusy() && robot.frontRight.isBusy() && robot.rearLeft.isBusy() && robot.rearRight.isBusy())) {

                drivingAngles = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
                telemetry.addData("First Angles", firstAngles.firstAngle);
                telemetry.addData("Moving Angles", drivingAngles.firstAngle);
                telemetry.update();
            }

            // Stop all motion;
            robot.frontLeft.setPower(0);
            robot.frontRight.setPower(0);
            robot.rearLeft.setPower(0);
            robot.rearRight.setPower(0);

            // Turn off RUN_TO_POSITION
            robot.frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.rearLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.rearRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            sleep(650);   // optional pause after each move
        }

    }
}
