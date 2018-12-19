package com.mrdagree.ftc.spr;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;


@TeleOp(name = "Jakes Testing Stuff", group = "")
@Disabled
public class JakeTesting extends LinearOpMode {
    DcMotor xrail;
    DcMotor liftMotor;
    CRServo intakeServo;

    double servopos;

    public void runOpMode() throws InterruptedException {
        xrail = hardwareMap.dcMotor.get("xrail");
        liftMotor = hardwareMap.dcMotor.get("liftMotor");
        intakeServo = hardwareMap.crservo.get("intakeServo");

        waitForStart();

        while (opModeIsActive()) {
            controls();
        }

    }

    public void controls() {
        liftMotor.setPower(-gamepad1.right_stick_y);
        xrail.setPower(-gamepad1.left_stick_y);

        intakeServo.setPower(servopos);


        if (gamepad1.right_bumper){
            liftMotor.setPower(0.1);
        }
        if (gamepad1.left_bumper){
            liftMotor.setPower(0.2);
        }


        servopos = (gamepad1.left_trigger);




    }

}