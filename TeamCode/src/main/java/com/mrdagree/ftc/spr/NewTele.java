package com.mrdagree.ftc.spr;


import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.Range;

@TeleOp (name = "Teleop")
public class NewTele extends LinearOpMode {

    Robot robot;
    //Variables
    public double flPower;
    public double frPower;
    public double rlPower;
    public double rrPower;
    public double intakePowerL;

    public double armPower;
    public double xrailPower;
    public RevBlinkinLedDriver.BlinkinPattern pattern;

    //Motor Variables
    public double leftStickY;
    public double rightStickX;
    public double leftStickX;
    public double flPowerraw;
    public double frPowerraw;
    public double rlPowerraw;
    public double rrPowerraw;
    public double leftPower;
    public double rightPower;


    @Override
    public void runOpMode() throws InterruptedException {

        robot = new Robot(hardwareMap);

        robot.markerServo.setPosition(0.8);
        pattern = RevBlinkinLedDriver.BlinkinPattern.RAINBOW_RAINBOW_PALETTE;



        telemetry.addData("Nothing will fall apart:", "Initialization Stage");
        telemetry.update();



        waitForStart();

        while (opModeIsActive()) {

            controls();
        }

    }

    public void controls() {

        //GAMEPAD 1 -- THE DRIVER

        if (gamepad1.left_bumper) {
            getJoyValuesReversed();
            pattern = RevBlinkinLedDriver.BlinkinPattern.HEARTBEAT_RED;

        } else {
            getJoyValues();
            pattern = RevBlinkinLedDriver.BlinkinPattern.RAINBOW_RAINBOW_PALETTE;

        }

        if (gamepad1.b) {
            robot.markerServo.setPosition(0);
        } else {
            robot.markerServo.setPosition(0.8);
        }

        lotsofcrap();
        wheelcrap();


        //GAMEPAD 2 -- THE SIDE BITCH


        if (gamepad2.left_trigger <= 0.05) {
            armPower = (gamepad2.right_trigger / 1.5);
        } else if (gamepad2.right_trigger <= 0.05) {
            armPower = (-gamepad2.left_trigger / 1.5);
        }
        if (gamepad2.left_bumper) {
            armPower = (0.2);
        }

        intakePowerL = gamepad2.right_stick_y;

        if (gamepad2.right_bumper) {
            robot.intakeHold.setPosition(1);
        } else {
            robot.intakeHold.setPosition(0);
        }

        if (gamepad1.right_trigger <= 0.05 && gamepad1.a) {
            robot.hangingMotor.setPower(gamepad1.left_trigger);
        } else {
            if (robot.topLift.getState() && robot.bottomLift.getState()) {
                if (gamepad1.right_trigger <= 0.05) {
                    robot.hangingMotor.setPower(gamepad1.left_trigger);
                }
                if (gamepad1.left_trigger <= 0.05) {
                    robot.hangingMotor.setPower(-gamepad1.right_trigger);
                }
            } else if (!robot.topLift.getState() && robot.bottomLift.getState()) {
                robot.hangingMotor.setPower(0.5);
                pattern = RevBlinkinLedDriver.BlinkinPattern.RED;
                sleep(150);
                robot.hangingMotor.setPower(0);
                sleep(500);
            } else if (robot.topLift.getState() && !robot.bottomLift.getState()) {
                robot.hangingMotor.setPower(-0.5);
                pattern = RevBlinkinLedDriver.BlinkinPattern.RED;
                sleep(150);
                robot.hangingMotor.setPower(0);
                sleep(500);
            }

            if (robot.topLift.getState() && robot.bottomLift.getState()) {
                if (gamepad1.right_trigger <= 0.05) {
                    robot.hangingMotor.setPower(gamepad1.left_trigger);
                }
                if (gamepad1.left_trigger <= 0.05) {
                    robot.hangingMotor.setPower(-gamepad1.right_trigger);
                }
            } else if (!robot.topLift.getState() && robot.bottomLift.getState()) {
                robot.hangingMotor.setPower(0.5);
                pattern = RevBlinkinLedDriver.BlinkinPattern.RED;
                sleep(150);
                robot.hangingMotor.setPower(0);
                sleep(500);
            } else if (robot.topLift.getState() && !robot.bottomLift.getState()) {
                robot.hangingMotor.setPower(-0.5);
                pattern = RevBlinkinLedDriver.BlinkinPattern.RED;
                sleep(150);
                robot.hangingMotor.setPower(0);
                sleep(500);
            }


            xrailPower = (gamepad2.left_stick_y);
            robot.xrailMotor.setPower(xrailPower);


            robot.armMotor.setPower(armPower);
            robot.armMotor2.setPower(armPower);
            robot.intakeServoL.setPower(intakePowerL);

            robot.theGoodStuff.setPattern(pattern);


            telemetry.addData("Waiting for Everything to Fall Apart:", "TeleOP Stage");
            telemetry.addData("LED PATTERN", pattern.toString());
            telemetry.update();

        }
    }

    public void getJoyValues() {
        leftStickY = gamepad1.left_stick_y;
        leftStickX = -gamepad1.left_stick_x;
        rightStickX = gamepad1.right_stick_x;
    }

    public void getJoyValuesReversed() {
        leftStickY = -gamepad1.left_stick_y;
        leftStickX = gamepad1.left_stick_x;
        rightStickX = -gamepad1.right_stick_x;
    }

    public void lotsofcrap() {
        flPowerraw = leftStickY - leftStickX - rightStickX;
        frPowerraw = leftStickY + leftStickX + rightStickX;
        rlPowerraw = leftStickY + leftStickX - rightStickX;
        rrPowerraw = leftStickY - leftStickX + rightStickX;

        flPower= Range.clip(flPowerraw, -1, 1);
        frPower = Range.clip(frPowerraw, -1, 1);
        rlPower = Range.clip(rlPowerraw,-1 ,1);
        rrPower = Range.clip(rrPowerraw, -1, 1);
    }
    public void wheelcrap() {
        robot.frontLeft.setPower(flPower);
        robot.frontRight.setPower(frPower);
        robot.rearLeft.setPower(rlPower);
        robot.rearRight.setPower(rrPower);
    }
    public void wheelcrapREVERSED(){
        robot.frontLeft.setPower(rrPower);
        robot.frontRight.setPower(rlPower);
        robot.rearLeft.setPower(frPower);
        robot.rearRight.setPower(flPower);
    }
}

