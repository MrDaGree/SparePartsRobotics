package com.mrdagree.ftc.spr;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcontroller.external.samples.SensorMRGyro;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

public class Robot {

    //Drive Motors
    public DcMotor frontRight;
    public DcMotor frontLeft;
    public DcMotor rearRight;
    public DcMotor rearLeft;

    //Intake Motors
    public DcMotor xrailMotor;
    public DcMotor armMotor;
    public DcMotor armMotor2;
    public Servo intakeHold;

    //OtherMotors
    public DcMotor hangingMotor;
    //Servos
    public Servo markerServo;
    public CRServo intakeServoL;

    //Range Sensors
    public DistanceSensor frontDistance;
    public DistanceSensor rightDistance;
    public DistanceSensor backDistance;

    //public DigitalChannel rightMagSwitch;
    public GyroSensor armGyro;

    // The IMU sensor object
    public BNO055IMU imu;

    public RevBlinkinLedDriver theGoodStuff;

    public Robot(HardwareMap hardwareMap){
        theGoodStuff = hardwareMap.get(RevBlinkinLedDriver.class, "ledDriver");

        //Drive Motors
        frontLeft = hardwareMap.dcMotor.get("lfWheel");
        frontRight = hardwareMap.dcMotor.get("rfWheel");
        rearLeft = hardwareMap.dcMotor.get("lrWheel");
        rearRight = hardwareMap.dcMotor.get("rrWheel");

        frontRight.setDirection(DcMotor.Direction.REVERSE);
        rearRight.setDirection(DcMotor.Direction.REVERSE);

        //Run without internal PID
        frontLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rearLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rearRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        //Intake Motors
        armMotor = hardwareMap.dcMotor.get("armMotor");
        armMotor2 = hardwareMap.dcMotor.get("armMotor2");

        //Other Motors
        hangingMotor = hardwareMap.dcMotor.get("hangingMotor");
        hangingMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        xrailMotor = hardwareMap.dcMotor.get("xrailMotor");
        xrailMotor.setDirection(DcMotorSimple.Direction.REVERSE);

        //Servos
        markerServo = hardwareMap.servo.get("markerServo");
        intakeServoL = hardwareMap.crservo.get("intakeServoL");
        intakeServoL.setDirection(CRServo.Direction.REVERSE);
        intakeHold = hardwareMap.servo.get("intakeHold");






        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "BNO055IMUCalibration.json"; // see the calibration sample opmode
        parameters.loggingEnabled      = true;
        parameters.loggingTag          = "IMU";
        parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();

        // Retrieve and initialize the IMU. We expect the IMU to be attached to an I2C port
        // on a Core Device Interface Module, configured to be a sensor of type "AdaFruit IMU",
        // and named "imu".
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);
    }


}
