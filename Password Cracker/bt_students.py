#!/usr/bin/env python

import py_trees as pt, py_trees_ros as ptr, rospy
from behaviours_student import *
from reactive_sequence import RSequence

class BehaviourTree(ptr.trees.BehaviourTree):

	def __init__(self):

		rospy.loginfo("Initialising behaviour tree")

		# tuck the arm
		b0 = tuckarm()

		# move the head down
		b1 = movehead("down")

		# pick the cube
		b2 = pick()

		# turn to the second table
		b3 = pt.composites.Selector(
			name="Go to table fallback",
			children=[counter(30, "Turned?"), go("Turn around!", 0, -1)]
		)

        # move the cube to the second table
		b4 = pt.composites.Selector(
			name="Go to table fallback",
			children=[counter(20, "At table?"), go("Go to table!", 1, 0)]
		)

		# place the cube
		b5 = place()


		# become the tree
		tree = RSequence(name="Main sequence", children=[b0, b1, b2, b3, b4, b5])
		super(BehaviourTree, self).__init__(tree)

		# execute the behaviour tree
		rospy.sleep(5)
		self.setup(timeout=10000)
		while not rospy.is_shutdown(): self.tick_tock(1)

class Detect(pt.behaviour.Behaviour):

    """
    Detects the cube location.
    """

    def __init__(self):

        rospy.loginfo("Initialising detect behaviour.")

        # server
        place_srv_nm = rospy.get_param(rospy.get_name() + '/place_srv')
        self.place_srv = rospy.ServiceProxy( place_srv_nm, SetBool)
        rospy.wait_for_service(place_srv_nm, timeout=30)

        # head movement direction; "down" or "up"
        self.direction = direction

        # execution checker
        self.tried = False
        self.done = False

        # become a behaviour
        super(detect, self).__init__("Place cube!")

    def update(self):

        # success if done
        if self.done:
            return pt.common.Status.SUCCESS

        # try if not tried
        elif not self.tried:

            # command
            self.move_head_req = self.move_head_srv(self.direction)
            self.tried = True

            # tell the tree you're running
            return pt.common.Status.RUNNING

        # if succesful
        elif self.move_head_req.success:
            self.done = True
            return pt.common.Status.SUCCESS

        # if failed
        elif not self.move_head_req.success:
            return pt.common.Status.FAILURE

        # if still trying
        else:
            return pt.common.Status.RUNNING


class pick(pt.behaviour.Behaviour):

    """
    Picks the cube off of the table.
    """

    def __init__(self):

        rospy.loginfo("Initialising pick behaviour.")

        # Initiating the service call
        pick_srv_nm = rospy.get_param(rospy.get_name() + '/pick_srv')
        self.pick_srv = rospy.ServiceProxy( pick_srv_nm, SetBool)
        rospy.wait_for_service(pick_srv_nm, timeout=30)

        # service response state
        self.picking = False
        self.done = False

        # become a behaviour
        super(pick, self).__init__("Pick cube!")

   def update(self):

        # success if done
        if self.done:
            return pt.common.Status.SUCCESS

        # try if not tried
        elif not self.picking:

            # command
            self.pick_req = self.pick_srv()
            self.picking = True

            # tell the tree you're running
            return pt.common.Status.RUNNING

        # if succesful
        elif self.pick_req.success:
            self.done = True
            return pt.common.Status.SUCCESS

        # if failed
        elif not self.pick_req.success:
            return pt.common.Status.FAILURE

        # if still trying
        else:
            return pt.common.Status.RUNNING



class place(pt.behaviour.Behaviour):

    """
    Places the cube on table.
    """

    def __init__(self, n, name):

        rospy.loginfo("Initialising place behaviour.")

        # Initiating the service call
        place_srv_nm = rospy.get_param(rospy.get_name() + '/place_srv')
        self.place_srv = rospy.ServiceProxy( place_srv_nm, SetBool)
        rospy.wait_for_service(place_srv_nm, timeout=30)


        # service response state
        self.placing = False
        self.done = False

        # become a behaviour
        super(place, self).__init__("Place cube!")

    def update(self):

        # success if done
        if self.done:
            return pt.common.Status.SUCCESS

        # try if not tried
        elif not self.placing:

            # command
            self.place_req = self.place_srv()
            self.placing = True

            # tell the tree you're running
            return pt.common.Status.RUNNING

        # if succesful
        elif self.place_req.success:
            self.done = True
            return pt.common.Status.SUCCESS

        # if failed
        elif not self.place_req.success:
            return pt.common.Status.FAILURE

        # if still trying
        else:
            return pt.common.Status.RUNNING

	
if __name__ == "__main__":


	rospy.init_node('main_state_machine')
	try:
		BehaviourTree()
	except rospy.ROSInterruptException:
		pass

	rospy.spin()